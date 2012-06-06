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
import static java.util.UUID.randomUUID;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.zentaur.http.Session;

/**
 * Default {@link Session} implementation
 */
final class DefaultSession
    implements Session
{

    private final UUID id = randomUUID();

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    private final Date creationTime = new Date();

    private Date lastAccessedTime = creationTime;

    private boolean isNew = true;

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getId()
    {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A> A getAttribute( String name )
    {
        Object attribute = attributes.get( name );
        if ( attribute != null )
        {
            @SuppressWarnings( "unchecked" ) // it would throw class cast exception anyway
            A value = (A) attribute;
            return value;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAttribute( String name )
    {
        attributes.remove( name );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <A> void setAttribute( String name, A value )
    {
        checkArgument( name != null, "Null attribute name not allowed" );
        attributes.put( name, value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getCreationTime()
    {
        return creationTime;
    }

    /**
     * Updates the session LastAccessedTime.
     */
    public synchronized void updateLastAccessedTime()
    {
        lastAccessedTime = new Date();
        isNew = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Date getLastAccessedTime()
    {
        return lastAccessedTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean isNew()
    {
        return isNew;
    }

    @Override
    public String toString()
    {
        return format( "Session[id=%s, attributes=%s, creationTime=%s, lastAccessedTime=%s, isNew=%s]",
                       id, attributes, creationTime, lastAccessedTime, isNew );
    }

}
