package org.zentaur.core.http.serialize;

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
import static java.nio.channels.Channels.newChannel;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.util.Locale.US;
import static org.zentaur.core.io.IOUtils.utf8ByteBuffer;
import static org.zentaur.http.Headers.CONTENT_ENCODING;
import static org.zentaur.http.Headers.CONTENT_LENGTH;
import static org.zentaur.http.Headers.CONTENT_TYPE;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPOutputStream;

import org.zentaur.core.io.ByteBufferEnqueuerOutputStream;
import org.zentaur.http.Cookie;
import org.zentaur.http.Response;

/**
 * Serializes an HTTP {@link Response} to the target output stream
 *
 * This class is not thread safe, create a new instance for each serialization.
 */
public final class ResponseSerializer
{

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz", US ); // RFC1123

    private static final String END_PADDING = "\r\n";

    private static final String GZIP = "gzip";

    private final Queue<ByteBuffer> responseBuffers = new ConcurrentLinkedQueue<ByteBuffer>();

    private final SelectionKey key;

    private final boolean gzipSupported;

    private Response response;

    /**
     * Creates a new serializer instance.
     *
     * @param key the selection key that currently holds the client/server connection.
     */
    public ResponseSerializer( SelectionKey key )
    {
        this( key, false );
    }

    /**
     * Creates a new serializer instance.
     *
     * @param key the selection key that currently holds the client/server connection.
     * @param gzipSupported flag to mark the client supports gzip compression.
     */
    public ResponseSerializer( SelectionKey key, boolean gzipSupported )
    {
        checkArgument( key != null, "Null SelectionKey not allowd." );
        this.key = key;
        this.gzipSupported = gzipSupported;
    }

    /**
     * Streams the input {@link Response} instance to the target output stream.
     *
     * @param response the response has to be serialized
     * @throws IOException if any error occurs while streaming
     */
    public void serialize( Response response )
        throws IOException
    {
        checkArgument( response != null, "Null Response cannot be serialized." );
        this.response = response;

        // print the body, so it will calculate the response size and populate the right HTTP header
        Queue<ByteBuffer> body = createBodyBuffer();

        // emit the protocol first
        emitProtocol();

        // key can start writing the protocol first
        key.attach( responseBuffers );
        key.interestOps( OP_WRITE );

        // headers are now complete
        emitHeaders();

        // cookies can go safety out
        emitCookies();

        // separate the head from the body
        responseBuffers.offer( utf8ByteBuffer( END_PADDING ) );

        // re-enqeue the body one piece at time
        // responseBuffers is under producer/consumer pattern
        while ( !body.isEmpty() )
        {
            responseBuffers.offer( body.remove() );
        }
    }

    /**
     * Writes the protocol reply.
     *
     * @throws IOException if any error occurs while streaming
     */
    private void emitProtocol()
        throws IOException
    {
        emit( "%s/%s %s %s%n",
               response.getProtocolName(),
               response.getProtocolVersion(),
               response.getStatus().getStatusCode(),
               response.getStatus().getStatusText() );
    }

    /**
     * Writes the HTTP Headers.
     *
     * @throws IOException if any error occurs while streaming
     */
    private void emitHeaders()
        throws IOException
    {
        for ( Entry<String, List<String>> header : response.getHeaders().getAllEntries() )
        {
            Formatter formatter = new Formatter().format( "%s: ", header.getKey() );

            int counter = 0;
            for ( String headerValue : header.getValue() )
            {
                formatter.format( "%s%s", ( counter++ > 0 ? ", " : "" ), headerValue );
            }

            formatter.format( "%n" );

            emit( formatter.toString() );
        }
    }

    /**
     * Writes the HTTP Cookies.
     *
     * @throws IOException if any error occurs while streaming
     */
    private void emitCookies()
        throws IOException
    {
        for ( Cookie cookie : response.getCookies() )
        {
            Formatter formatter = new Formatter()
                                     .format( "Set-Cookie: %s=%s; Path=%s; Domain=%s;",
                                              cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain() );

            if ( !cookie.getPorts().isEmpty() )
            {
                formatter.format( " Port=\"" );
                int i = 0;
                for ( Integer port : cookie.getPorts() )
                {
                    formatter.format( "%s%s", ( i++ > 0 ? "," : "" ), port );
                }
                formatter.format( "\";" );
            }

            if ( cookie.getMaxAge() != -1 )
            {
                Date expirationDate = new Date( cookie.getMaxAge() * 1000 + currentTimeMillis() );
                String expires = dateFormat.format( expirationDate );

                formatter.format( " Expires=%s;", expires );
            }

            // secure field ignored since HTTPs is not supported in this version

            emit( formatter.format( " HttpOnly%n" ).toString() );
        }
    }

    /**
     * Creates the response body, splitted in chunks (body can be also very large)
     * and counts the bytes size, then sets the right Content-Length HTTP header.
     *
     * @throws IOException if any error occurs while streaming
     */
    private Queue<ByteBuffer> createBodyBuffer()
        throws IOException
    {
        final Queue<ByteBuffer> bodyBuffers = new LinkedList<ByteBuffer>();

        if ( response.getBodyWriter().contentType() != null )
        {
            response.addHeader( CONTENT_TYPE, response.getBodyWriter().contentType() );
        }

        ByteBufferEnqueuerOutputStream target = new ByteBufferEnqueuerOutputStream( bodyBuffers );

        if ( gzipSupported )
        {
            response.addHeader( CONTENT_ENCODING, GZIP );
            GZIPOutputStream gzipTarget = new GZIPOutputStream( target );

            response.getBodyWriter().write( newChannel( gzipTarget ) );

            gzipTarget.finish();
        }
        else
        {
            response.getBodyWriter().write( newChannel( target ) );
        }

        target.flush();
        target.close();

        long writtenBytes = target.getWrittenBytes();

        if ( writtenBytes > 0 )
        {
            response.addHeader( CONTENT_LENGTH, String.valueOf( writtenBytes ) );
        }

        return bodyBuffers;
    }

    /**
     * Generic method to emit UTF message and enqueues the resultant chunk in the response queue.
     *
     * @param messageTemplate the String message format
     * @param args the message template placeholders variables
     * @throws IOException if any error occurs while streaming
     */
    private void emit( String messageTemplate, Object...args )
        throws IOException
    {
        responseBuffers.offer( utf8ByteBuffer( format( messageTemplate, args ) ) );
    }

}
