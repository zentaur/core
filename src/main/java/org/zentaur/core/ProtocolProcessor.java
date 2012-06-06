package org.zentaur.core;

/*
 *   Copyright 2012 The Zentaur Server Project
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.zentaur.core.http.ResponseFactory.newResponse;
import static org.zentaur.http.Headers.ACCEPT_ENCODING;
import static org.zentaur.http.Headers.CONNECTION;
import static org.zentaur.http.Headers.DATE;
import static org.zentaur.http.Headers.KEEP_ALIVE;
import static org.zentaur.http.Headers.SERVER;
import static org.zentaur.http.Response.Status.INTERNAL_SERVER_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;

import org.zentaur.core.http.SessionManager;
import org.zentaur.core.http.serialize.ResponseSerializer;
import org.zentaur.http.Cookie;
import org.zentaur.http.Request;
import org.zentaur.http.Response;
import org.slf4j.Logger;

/**
 * Asynchronous HTTP protocol processor to serve current request.
 */
final class ProtocolProcessor
    implements Runnable
{

    private static final Logger logger = getLogger( ProtocolProcessor.class );

    private static final String DEFAULT_SERVER_NAME = "Simple HttpServer";

    private static final String GZIP = "gzip";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz" );

    private final SessionManager sessionManager;

    private final RequestDispatcher requestDispatcher;

    private final Request request;

    private SelectionKey key;

    public ProtocolProcessor( SessionManager sessionManager,
                              RequestDispatcher requestDispatcher,
                              Request request,
                              SelectionKey key )
    {
        this.sessionManager = sessionManager;
        this.requestDispatcher = requestDispatcher;
        this.request = request;
        this.key = key;
    }

    public void run()
    {
        long start = currentTimeMillis();

        // debug the request
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "{} < {} {} {}/{}", new Object[] {
                                                  request.getClientHost(),
                                                  request.getMethod(),
                                                  request.getPath(),
                                                  request.getProtocolName(),
                                                  request.getProtocolVersion()
                                              } );

            for ( Entry<String, List<String>> header : request.getHeaders().getAllEntries() )
            {
                Formatter headerValues = new Formatter();
                int i = 0;
                for ( String value : header.getValue() )
                {
                    headerValues.format( "%s%s", ( i++ > 0 ? ", " : "" ), value );
                }

                logger.debug( "{} < {}: {}", new Object[] {
                                                 request.getClientHost(),
                                                 header.getKey(),
                                                 headerValues.toString()
                                             } );
            }

            if ( !request.getCookies().isEmpty() )
            {
                Formatter cookies = new Formatter();
                int counter = 0;
                for ( Cookie cookie : request.getCookies() )
                {
                    cookies.format( "%s%s=%s", ( counter++ > 0 ? ", " : "" ), cookie.getName(), cookie.getValue() );
                }
                logger.debug( "{} < Cookie: {}", request.getClientHost(), cookies.toString() );
            }
        }

        Response response = newResponse();
        response.addHeader( DATE, dateFormat.format( new Date() ) );
        response.addHeader( SERVER, DEFAULT_SERVER_NAME );

        SocketChannel serverChannel = (SocketChannel) key.channel();
        Socket socket = serverChannel.socket();
        try
        {
            if ( socket.getKeepAlive() )
            {
                response.addHeader( KEEP_ALIVE, format( "timeout=%s", socket.getSoTimeout() / 1000 ) );
                response.addHeader( CONNECTION, KEEP_ALIVE );
            }
        }
        catch ( SocketException e )
        {
            // just ignore the Keep-Alive option
        }

        try
        {
            sessionManager.manageSession( request, response );
            requestDispatcher.dispatch( request, response );

            response.setProtocolName( request.getProtocolName() );
            response.setProtocolVersion( request.getProtocolVersion() );
        }
        catch ( IOException e )
        {
            logger.error( "Request cannot be satisfied due to internal I/O error", e );

            response.setStatus( INTERNAL_SERVER_ERROR );
        }
        finally
        {
            boolean gzipEnabled = request.getHeaders().contains( ACCEPT_ENCODING )
                                  && request.getHeaders().getValues( ACCEPT_ENCODING ).contains( GZIP );

            try
            {
                new ResponseSerializer( key, gzipEnabled ).serialize( response );
            }
            catch ( IOException e )
            {
                key.cancel();

                logger.error( "Impossible to stream Response to the client", e );
            }

            // debug the response
            if ( logger.isDebugEnabled() )
            {
                // protocol
                logger.debug( "{} > {}/{} {} {}",
                              new Object[] {
                                  request.getClientHost(),
                                  response.getProtocolName(),
                                  response.getProtocolVersion(),
                                  response.getStatus().getStatusCode(),
                                  response.getStatus().getStatusText()
                              } );
                // headers
                for ( Entry<String, List<String>> header : response.getHeaders().getAllEntries() )
                {
                    Formatter headerValues = new Formatter();

                    int counter = 0;
                    for ( String headerValue : header.getValue() )
                    {
                        headerValues.format( "%s%s", ( counter++ > 0 ? ", " : "" ), headerValue );
                    }

                    logger.debug( "{} > {}: {}", new Object[] {
                        request.getClientHost(),
                        header.getKey(),
                        headerValues.toString()
                    } );
                }
                // cookies
                for ( Cookie cookie : response.getCookies() )
                {
                    Formatter cookieFormatter = new Formatter()
                                             .format( "%s=%s; Path=%s; Domain=%s;",
                                                      cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain() );

                    if ( !cookie.getPorts().isEmpty() )
                    {
                        cookieFormatter.format( " Port=\"" );
                        int i = 0;
                        for ( Integer port : cookie.getPorts() )
                        {
                            cookieFormatter.format( "%s%s", ( i++ > 0 ? "," : "" ), port );
                        }
                        cookieFormatter.format( "\";" );
                    }

                    if ( cookie.getMaxAge() != -1 )
                    {
                        Date expirationDate = new Date( cookie.getMaxAge() * 1000 + currentTimeMillis() );
                        String expires = dateFormat.format( expirationDate );

                        cookieFormatter.format( " Expires=%s;", expires );
                    }

                    // secure field ignored since HTTPs is not supported in this version

                    logger.debug( "{} > Set-Cookie: {} HttpOnly", request.getClientHost(), cookieFormatter.toString() );
                }

                if ( logger.isInfoEnabled() )
                {
                    logger.info( "Request process completed in {}ms", ( currentTimeMillis() - start ) );
                }
            }
        }
    }

}
