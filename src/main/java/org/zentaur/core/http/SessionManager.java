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

import static java.util.UUID.fromString;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.zentaur.http.Cookie;
import org.zentaur.http.Request;
import org.zentaur.http.Response;

/**
 * The SessionRegistry is the HTTP {@link org.zentaur.http.Session} manager.
 */
public final class SessionManager
{

    private static final String SESSION_NAME = "SHSSESSIONID";

    /**
     * The registry where sessions have to be stored.
     */
    private final ConcurrentMap<UUID, DefaultSession> sessionsRegistry = new ConcurrentHashMap<UUID, DefaultSession>();

    /**
     * The timer delegated to clean the expired sessions.
     */
    private final Timer sessionsCleaner = new Timer( true );

    /**
     * The sessions max age (in milliseconds).
     */
    private final int sessionMaxAge;

    /**
     *
     * @param sessionMaxAge the max
     */
    public SessionManager( int sessionMaxAge )
    {
        this.sessionMaxAge = sessionMaxAge;
    }

    /**
     * Manages the request session: if the client sends a request which contains
     * the session id that is currently managed by the server (and didn't expire)
     * then will be retrieved and restored, otherwise it will create a new one.
     *
     * @param request the received HTTP request
     * @param response the HTTP response will be pushed out.
     */
    public void manageSession( Request request, Response response )
    {
        DefaultSession session = null;

        // check first the session is present in the registry

        Iterator<Cookie> cookies = request.getCookies().iterator();
        while ( session != null || cookies.hasNext() )
        {
            Cookie cookie = cookies.next();
            if ( SESSION_NAME.equals( cookie.getName() ) )
            {
                UUID sessionId = fromString( SESSION_NAME );
                session = sessionsRegistry.get( sessionId );
                if ( session != null )
                {
                    session.updateLastAccessedTime();
                }
            }
        }

        // creates a new session, puts it in the registry and schedule for deletion

        if ( session == null )
        {
            session = new DefaultSession();

            sessionsRegistry.put( session.getId(), session );

            sessionsCleaner.schedule( new SessionRemoverTimerTask( sessionsRegistry, session.getId() ), sessionMaxAge );

            response.addCookie( new CookieBuilder()
                                    .setDomain( request.getServerHost() )
                                    .addPort( request.getServerPort() )
                                    .setMaxAge( sessionMaxAge )
                                    .setName( SESSION_NAME )
                                    .setValue( session.getId().toString() )
                                    .setPath( "/" )
                                    .build() );
        }

        ( (MutableRequest) request ).setSession( session );
    }

    /**
     * Turns off the timer to clean the registered sessions.
     */
    public void shutDown()
    {
        sessionsCleaner.cancel();
    }

    /**
     * A timer task to remove sessions from registry when expired.
     */
    private static final class SessionRemoverTimerTask
        extends TimerTask
    {

        /**
         * The registry where session has to be removed from.
         */
        private final ConcurrentMap<UUID, DefaultSession> sessionsRegistry;

        /**
         * The session id to remove.
         */
        private final UUID sessionId;

        /**
         * Creates a new task to remove sessions from registry when expired.
         *
         * @param sessionsRegistry the registry where session has to be removed from
         * @param sessionId the session id to remove
         */
        public SessionRemoverTimerTask( ConcurrentMap<UUID, DefaultSession> sessionsRegistry, UUID sessionId )
        {
            this.sessionsRegistry = sessionsRegistry;
            this.sessionId = sessionId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run()
        {
            sessionsRegistry.remove( sessionId );
        }

    }

}
