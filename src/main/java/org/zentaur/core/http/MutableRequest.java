package org.zentaur.core.http;

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

import static org.zentaur.core.io.ByteBufferEnqueuerOutputStream.EOM;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.zentaur.collections.MultiValued;
import org.zentaur.core.collections.SimpleMultiValued;
import org.zentaur.http.Cookie;
import org.zentaur.http.Request;
import org.zentaur.http.Session;
import org.zentaur.io.RequestBodyReader;
import org.zentaur.io.StreamAlreadyConsumedException;

/**
 * Basic {@link Request} implementation.
 */
public final class MutableRequest
    implements Request
{

    private String clientHost;

    private String serverHost;

    private int serverPort;

    private Method method;

    private String path;

    private String protocolName;

    private String protocolVersion;

    private long contentLength = -1;

    private Queue<ByteBuffer> requestBody;

    private Session session;

    private final SimpleMultiValued<String, String> headers = new SimpleMultiValued<String, String>();

    private final SimpleMultiValued<String, String> queryStringParameters = new SimpleMultiValued<String, String>();

    private final SimpleMultiValued<String, String> parameters = new SimpleMultiValued<String, String>();

    private final List<Cookie> cookies = new LinkedList<Cookie>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientHost()
    {
        return clientHost;
    }

    /**
     * Sets the client host connected to this server.
     *
     * @param clientHost the client host connected to this server.
     */
    public void setClientHost( String clientHost )
    {
        checkArgument( clientHost != null, "Null clientHost not allowed" );
        this.clientHost = clientHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerHost()
    {
        return serverHost;
    }

    /**
     * Sets this server host.
     *
     * @param serverHost this server host.
     */
    public void setServerHost( String serverHost )
    {
        checkArgument( serverHost != null, "Null serverHost not allowed" );
        this.serverHost = serverHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getServerPort()
    {
        return serverPort;
    }

    /**
     * Sets this server port.
     *
     * @param serverPort this server port.
     */
    public void setServerPort( int serverPort )
    {
        checkArgument( serverPort > 0, "Negative or zero serverPort not allowed" );
        this.serverPort = serverPort;
    }

    /**
     * {@inheritDoc}
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the request path.
     *
     * @param path the request path.
     */
    public void setPath( String path )
    {
        checkArgument( path != null, "Null path not allowed" );
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod()
    {
        return method;
    }

    /**
     * Set the HTTP Method
     *
     * @param method the HTTP Method
     */
    public void setMethod( Method method )
    {
        checkArgument( method != null, "Null method not allowed" );
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocolName()
    {
        return protocolName;
    }

    /**
     * Sets the protocol name.
     *
     * @param protocolName the protocol name.
     */
    public void setProtocolName( String protocolName )
    {
        checkArgument( protocolName != null, "Null protocolName name not allowed" );
        this.protocolName = protocolName;
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocolVersion()
    {
        return protocolVersion;
    }

    /**
     * Sets the protocol version.
     *
     * @param protocolVersion the protocol version.
     */
    public void setProtocolVersion( String protocolVersion )
    {
        checkArgument( protocolVersion != null, "Null protocolVersion name not allowed" );
        this.protocolVersion = protocolVersion;
    }

    /**
     * Allows adding a new HTTP Header.
     *
     * @param name a non null Header name
     * @param value a non null Header value
     */
    public void addHeader( String name, String value )
    {
        checkArgument( name != null, "Null Header name not allowed" );
        checkArgument( value != null, "Null Header values not allowed" );

        headers.addValue( name, value );
    }

    /**
     * {@inheritDoc}
     */
    public MultiValued<String, String> getHeaders()
    {
        return headers;
    }

    /**
     * Add a non null cookie.
     *
     * @param cookie a non null cookie.
     */
    public void addCookie( Cookie cookie )
    {
        checkArgument( cookie != null, "Null cookie name not allowed" );
        cookies.add( cookie );
    }

    /**
     * {@inheritDoc}
     */
    public List<Cookie> getCookies()
    {
        return unmodifiableList( cookies );
    }

    /**
     * Allows adding a new query string parameter.
     *
     * @param name a non null query string parameter name
     * @param value a non null query string parameter value
     */
    public void addQueryStringParameter( String name, String value )
    {
        checkArgument( name != null, "Null QueryStringParameter name not allowed" );
        checkArgument( value != null, "Null QueryStringParameter values not allowed" );

        queryStringParameters.addValue( name, value );
    }

    /**
     * {@inheritDoc}
     */
    public MultiValued<String, String> getQueryStringParameters()
    {
        return queryStringParameters;
    }

    /**
     * Allows adding a new parameter.
     *
     * @param name a non null parameter name
     * @param value a non null parameter value
     */
    public void addParameter( String name, String value )
    {
        checkArgument( name != null, "Null Parameter name not allowed" );
        checkArgument( value != null, "Null Parameter values not allowed" );

        parameters.addValue( name, value );
    }

    /**
     * {@inheritDoc}
     */
    public MultiValued<String, String> getParameters()
    {
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getContentLength()
    {
        return contentLength;
    }

    /**
     * Sets the request body content length.
     *
     * @param contentLength the request body content length.
     */
    public void setContentLength( long contentLength )
    {
        checkArgument( contentLength >= 0, "Negative contentLength not allowed" );
        this.contentLength = contentLength;
    }

    /**
     * Set the buffered request content body.
     *
     * @param requestBody the request content body.
     */
    public void setRequestBody( Queue<ByteBuffer> requestBody )
    {
        checkArgument( requestBody != null, "Null requestBody not allowed" );
        this.requestBody = requestBody;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T readRequestBody( RequestBodyReader<T> requestBodyReader )
        throws IOException
    {
        checkArgument( requestBodyReader != null, "Null requestBodyReader not allowed" );

        if ( requestBody == null || requestBody.isEmpty() )
        {
            throw new StreamAlreadyConsumedException();
        }

        // read the request body in a circular queue,
        // so users can read it as many times they like
        ByteBuffer current;

        while ( EOM != ( current = requestBody.remove() ) )
        {
            requestBodyReader.onBodyPartReceived( current );
            current.rewind();
            requestBody.offer( current );
        }

        requestBody.offer( EOM );

        return requestBodyReader.onComplete();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session getSession()
    {
        return session;
    }

    /**
     * Sets the current user session.
     *
     * @param session the current user session.
     */
    public void setSession( Session session )
    {
        checkArgument( session != null, "Null session not allowed" );
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return format( "Request [clientHost=%s, serverHost=%s, serverPort=%s, method=%s, path=%s, protocolName=%s, protocolVersion=%s, headers=%s, cookies=%s, queryStringParameters=%s, parameters=%s]",
                       clientHost, serverHost, serverPort, method, path, protocolName, protocolVersion, headers, cookies, queryStringParameters, parameters );
    }

}
