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

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.LinkedList;

import org.zentaur.collections.MultiValued;
import org.zentaur.core.collections.SimpleMultiValued;
import org.zentaur.http.Cookie;
import org.zentaur.http.Response;
import org.zentaur.io.ResponseBodyWriter;

/**
 * Basic {@link Response} implementation.
 */
final class DefaultResponse
    implements Response
{

    private static final String DEFAULT_PROTOCOL_NAME = "HTTP";

    private static final String DEFAULT_PROTOCOL_VERSION = "1.1";

    private Status status;

    private String protocolName = DEFAULT_PROTOCOL_NAME;

    private String protocolVersion = DEFAULT_PROTOCOL_VERSION;

    private final SimpleMultiValued<String, String> headers = new SimpleMultiValued<String, String>();

    private final Collection<Cookie> cookies = new LinkedList<Cookie>();

    private ResponseBodyWriter bodyWriter = new NoOpResponseBodyWriter();

    public void setStatus( Status status )
    {
        checkArgument( status != null, "Null status not allowed in HTTP Response." );
        this.status = status;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setProtocolName( String protocolName )
    {
        checkArgument( protocolName != null, "Null protocolName not allowed in HTTP Response." );
        this.protocolName = protocolName;
    }

    public String getProtocolName()
    {
        return protocolName;
    }

    public String getProtocolVersion()
    {
        return protocolVersion;
    }

    public void setProtocolVersion( String protocolVersion )
    {
        checkArgument( protocolVersion != null, "Null protocolVersion cannot be set" );
        this.protocolVersion = protocolVersion;
    }

    public void addHeader( String name, String value )
    {
        checkArgument( name != null, "Null Header name not allowed" );
        checkArgument( value != null, "Null Header values not allowed" );

        headers.addValue( name, value );
    }

    public MultiValued<String, String> getHeaders()
    {
        return headers;
    }

    public void addCookie( Cookie cookie )
    {
        checkArgument( cookie != null, "Null Cookie cannot be added" );
        cookies.add( cookie );
    }

    public Collection<Cookie> getCookies()
    {
        return unmodifiableCollection( cookies );
    }

    public ResponseBodyWriter getBodyWriter()
    {
        return bodyWriter;
    }

    public void setBody( ResponseBodyWriter bodyWriter )
    {
        checkArgument( bodyWriter != null, "Null bodyWriter cannot be added" );
        this.bodyWriter = bodyWriter;
    }

    @Override
    public String toString()
    {
        return format( "Response [status=%s, protocolName=%s, protocolVersion=%s, headers=%s]",
                       status, protocolName, protocolVersion, headers );
    }

    /**
     * NO-OP {@link ResponseBodyWriter} implementation.
     */
    private static final class NoOpResponseBodyWriter
        implements ResponseBodyWriter
    {

        @Override
        public String contentType()
        {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void write( WritableByteChannel output )
            throws IOException
        {
            // do nothing
        }

    }

}
