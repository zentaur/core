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

import static org.zentaur.http.Response.Status.NOT_FOUND;
import static org.zentaur.http.Response.Status.OK;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.zentaur.core.io.FileResponseBodyWriter;
import org.zentaur.http.Request;
import org.zentaur.http.RequestHandler;
import org.zentaur.http.Response;
import org.zentaur.http.Response.Status;
import org.slf4j.Logger;

/**
 * The request dispatcher is the responsible to address requests to the right {@link RequestHandler}
 */
final class RequestDispatcher
{

    private final Logger logger = getLogger( getClass() );

    private final List<MatchingRequestHandler> handlers = new LinkedList<MatchingRequestHandler>();

    private final Map<Status, File> defaultResponses = new EnumMap<Status, File>( Status.class );

    public void addRequestHandler( String path, RequestHandler requestHandler )
    {
        handlers.add( new MatchingRequestHandler( path, requestHandler ) );
    }

    public void addDefaultResponse( Status status, File defaultReply )
    {
        defaultResponses.put( status, defaultReply );
    }

    public void dispatch( Request request, Response response )
        throws IOException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Choosing the right handler to dispatch {} request...", request.getPath() );
        }

        boolean found = false;
        Iterator<MatchingRequestHandler> handlersIterator = handlers.iterator();

        while ( !found && handlersIterator.hasNext() )
        {
            MatchingRequestHandler handler = handlersIterator.next();

            if ( handler.shouldServe( request.getPath() ) )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "Request {} will be dispatched by {}", request.getPath(),
                                  handler.getRequestHandler().getClass().getName() );
                }

                // found right handler to address the request
                response.setStatus( OK );

                // exception can be thrown by the method - loop would be blocked anyway
                handler.getRequestHandler().handle( request, response );
                // return the server request
                found = true;
            }
        }

        if ( !found )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "No handler found for path {}, request will just return NOT_FOUND", request.getPath() );
            }

            response.setStatus( NOT_FOUND );
        }

        // check a default response has to be provided

        File defaultResponse = defaultResponses.get( response.getStatus() );
        if ( defaultResponse != null )
        {
            logger.info( "Default response {} configured to reply to status {}", defaultResponse, response.getStatus() );
            response.setBody( new FileResponseBodyWriter( defaultResponse ) );
        }
        else if ( logger.isDebugEnabled() )
        {
            logger.debug( "No default response configured to reply to status {}", response.getStatus() );
        }
    }

    /**
     * Matches URIs using the pattern grammar of the Servlet API and web.xml.
     *
     * This class has been borrowed from <a href="http://code.google.com/p/google-guice">Google Guice</a>,
     * see the original <a href="http://code.google.com/p/google-guice/source/browse/extensions/servlet/src/com/google/inject/servlet/UriPatternType.java">source</a>
     */
    private static final class MatchingRequestHandler
    {

        private static enum Kind
        {
            PREFIX,
            SUFFIX,
            LITERAL;
        }

        private final String pattern;

        private final Kind patternKind;

        private final RequestHandler requestHandler;

        public MatchingRequestHandler( String pattern, RequestHandler requestHandler )
        {
            if ( pattern.startsWith( "*" ) )
            {
                this.pattern = pattern.substring( 1 );
                this.patternKind = Kind.PREFIX;
            }
            else if ( pattern.endsWith( "*" ) )
            {
                this.pattern = pattern.substring( 0, pattern.length() - 1 );
                this.patternKind = Kind.SUFFIX;
            }
            else
            {
                this.pattern = pattern;
                this.patternKind = Kind.LITERAL;
            }
            this.requestHandler = requestHandler;
        }

        public boolean shouldServe( String uri )
        {
            if ( null == uri )
            {
                return false;
            }

            if ( patternKind == Kind.PREFIX )
            {
                return uri.endsWith( pattern );
            }
            else if ( patternKind == Kind.SUFFIX )
            {
                return uri.startsWith( pattern );
            }

            // else treat as a literal
            return pattern.equals( extractPath( uri ) );
        }

        public RequestHandler getRequestHandler()
        {
            return requestHandler;
        }

        private String extractPath( String path )
        {
            if ( patternKind == Kind.PREFIX )
            {
                return null;
            }
            else if ( patternKind == Kind.SUFFIX )
            {
                String extract = pattern;

                // trim the trailing '/'
                if ( extract.endsWith( "/" ) )
                {
                    extract = extract.substring( 0, extract.length() - 1 );
                }

                return extract;
            }

            // else treat as literal
            return path;
        }

    }

}
