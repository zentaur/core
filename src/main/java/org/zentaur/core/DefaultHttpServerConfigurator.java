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

import static org.zentaur.lang.Preconditions.checkArgument;

import java.io.File;

import org.zentaur.DefaultResponseBuilder;
import org.zentaur.HttpServerConfigurator;
import org.zentaur.RequestHandlerBuilder;
import org.zentaur.http.RequestHandler;
import org.zentaur.http.Response.Status;

/**
 * Default {@link HttpServerConfigurator} implementation
 */
final class DefaultHttpServerConfigurator
    implements HttpServerConfigurator
{

    private String host;

    private int port;

    private int threads;

    private int sessionMaxAge;

    private int keepAliveTimeOut;

    private final RequestDispatcher requestDispatcher = new RequestDispatcher();

    /**
     * The host name or the textual representation of its IP address.
     *
     * @return the host name or the textual representation of its IP address.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindServerToHost( String host )
    {
        this.host = host;
    }

    /**
     * The port number where binding the server.
     *
     * @return the port number where binding the server.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindServerToPort( int port )
    {
        this.port = port;
    }

    /**
     * The number of threads that will serve the HTTP requests.
     *
     * @return the number of threads that will serve the HTTP requests.
     */
    public int getThreads()
    {
        return threads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serveRequestsWithThreads( int threads )
    {
        this.threads = threads;
    }

    /**
     * The maximum number of seconds of life of HTTP Sessions.
     *
     * @return the maximum number of seconds of life of HTTP Sessions.
     */
    public int getSessionMaxAge()
    {
        return sessionMaxAge;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionsHaveMagAge( int sessionMaxAge )
    {
        this.sessionMaxAge = sessionMaxAge;
    }

    public int getKeepAliveTimeOut()
    {
        return keepAliveTimeOut;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keepAliveConnectionsHaveTimeout( int keepAliveTimeOut )
    {
        checkArgument( keepAliveTimeOut >= 0, "Negative connection keep alive timeout not allowed" );
        this.keepAliveTimeOut = keepAliveTimeOut * 1000;
    }

    public RequestDispatcher getRequestDispatcher()
    {
        return requestDispatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestHandlerBuilder serve( final String path )
    {
        checkArgument( path != null, "Null path cannot be served." );
        checkArgument( !path.isEmpty(), "Empty path not allowed." );

        return new RequestHandlerBuilder()
        {

            public void with( final RequestHandler requestHandler )
            {
                checkArgument( requestHandler != null, "Null requestHandler not allowed." );

                requestDispatcher.addRequestHandler( path, requestHandler );
            }

        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DefaultResponseBuilder when( final Status status )
    {

        checkArgument( status != null, "Null status cannot be served." );

        return new DefaultResponseBuilder()
        {

            @Override
            public void serve( File defaultReply )
            {
                checkArgument( defaultReply != null, "Null defaultReply cannot provide reply for %s.", status );
                checkArgument( defaultReply.exists(), "Cannot provide defaultReply reply for %s because file %s doesn't exist.", status, defaultReply );
                checkArgument( defaultReply.isFile(), "Cannot provide defaultReply reply for %s because file %s is not a regular file.", status, defaultReply );

                requestDispatcher.addDefaultResponse( status, defaultReply );
            }

        };
    }

}
