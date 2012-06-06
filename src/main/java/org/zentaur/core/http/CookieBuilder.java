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
import static java.util.Collections.unmodifiableSet;
import static org.zentaur.lang.Objects.eq;
import static org.zentaur.lang.Objects.hash;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.Set;

import org.zentaur.http.Cookie;

/**
 * A builder to simplify the {@link Cookie} instantiation.
 */
public final class CookieBuilder
{

    private String domain;

    private String name;

    private String value;

    private String path;

    private int maxAge = -1;

    private boolean secure = false;

    private Set<Integer> ports = new HashSet<Integer>();

    /**
     * Set the cookie domain - must be not null.
     *
     * @param domain the cookie domain
     * @return this builder instance
     */
    public CookieBuilder setDomain( String domain )
    {
        checkArgument( domain != null, "Cookie domain must be not null" );
        checkArgument( !domain.isEmpty(), "Cookie domain must be not empty" );
        this.domain = domain;
        return this;
    }

    /**
     * Set the cookie name - must be not null.
     *
     * @param name the cookie name
     * @return this builder instance
     */
    public CookieBuilder setName( String name )
    {
        checkArgument( name != null, "Cookie name must be not null" );
        checkArgument( !name.isEmpty(), "Cookie name must be not empty" );
        this.name = name;
        return this;
    }

    /**
     * Set the cookie value - must be not null.
     *
     * @param value the cookie value
     * @return this builder instance
     */
    public CookieBuilder setValue( String value )
    {
        checkArgument( value != null, "Cookie value must be not null" );
        checkArgument( !value.isEmpty(), "Cookie value must be not empty" );
        this.value = value;
        return this;
    }

    /**
     * Set the cookie path - must be not null.
     *
     * @param path the cookie path
     * @return this builder instance
     */
    public CookieBuilder setPath( String path )
    {
        checkArgument( path != null, "Cookie path must be not null" );
        checkArgument( !path.isEmpty(), "Cookie path must be not empty" );
        this.path = path;
        return this;
    }

    /**
     * Set the cookie max age - consider using -1 for non expiration.
     *
     * @param maxAge the cookie max age
     * @return this builder instance
     */
    public CookieBuilder setMaxAge( int maxAge )
    {
        checkArgument( maxAge >= -1, "Cookie maxAge must be not less than -1" );
        this.maxAge = maxAge;
        return this;
    }

    /**
     * Set the flag to mark the cookie is secure.
     *
     * @param secure the flag to mark the cookie is secure
     * @return this builder instance
     */
    public CookieBuilder setSecure( boolean secure )
    {
        this.secure = secure;
        return this;
    }

    /**
     * Add a cookie port - must be not 0 nor negative.
     *
     * @param port a cookie port
     * @return this builder instance
     */
    public CookieBuilder addPort( int port )
    {
        checkArgument( port > 0, "Cookie port must be not 0 nor negative" );
        this.ports.add( port );
        return this;
    }

    /**
     * Creates a new {@link Cookie} instance given the set parameters.
     *
     * @return a new {@link Cookie} instance.
     */
    public Cookie build()
    {
        return new DefaultCookie( domain, name, value, path, maxAge, secure, ports );
    }

    /**
     * Default cookie implementation.
     */
    private static final class DefaultCookie
        implements Cookie
    {

        private final String domain;

        private final String name;

        private final String value;

        private final String path;

        private final int maxAge;

        private final boolean secure;

        private final Set<Integer> ports;

        private DefaultCookie( String domain,
                               String name,
                               String value,
                               String path,
                               int maxAge,
                               boolean secure,
                               Set<Integer> ports )
        {
            this.domain = domain;
            this.name = name;
            this.value = value;
            this.path = path;
            this.maxAge = maxAge;
            this.secure = secure;
            this.ports = ports;
        }

        /**
         * {@inheritDoc}
         */
        public String getDomain()
        {
            return domain;
        }

        /**
         * {@inheritDoc}
         */
        public String getName()
        {
            return name;
        }

        /**
         * {@inheritDoc}
         */
        public String getValue()
        {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        public String getPath()
        {
            return path;
        }

        /**
         * {@inheritDoc}
         */
        public int getMaxAge()
        {
            return maxAge;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isSecure()
        {
            return secure;
        }

        /**
         * {@inheritDoc}
         */
        public Set<Integer> getPorts()
        {
            return unmodifiableSet( ports );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode()
        {
            return hash( 1, 31, domain, maxAge, name, path, ports, secure, value );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }

            if ( obj == null || getClass() != obj.getClass() )
            {
                return false;
            }

            DefaultCookie other = (DefaultCookie) obj;
            return eq( domain, other.getDomain() )
                && eq( maxAge, other.getMaxAge() )
                && eq( name, other.getName() )
                && eq( path, other.getPath() )
                && eq( ports, other.getPorts() )
                && eq( secure, other.isSecure() )
                && eq( value, other.getValue() );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            return format( "Cookie[domain=%s, name=%s, value=%s, path=%s, maxAge=%s, secure=%s, ports=%s]",
                           domain, name, value, path, maxAge, secure, ports );
        }

    }

}
