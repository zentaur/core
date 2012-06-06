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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Custom {@link ThreadFactory} that creates daemon threads with proper name.
 */
final class ProtocolProcessorThreadFactory
    implements ThreadFactory
{

    private static final String THREAD_NAME_FORMAT = "protocol-processor-%s";

    private final AtomicLong count = new AtomicLong( 0 );

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread( Runnable runnable )
    {
        Thread thread = new Thread( runnable );
        thread.setName( format( THREAD_NAME_FORMAT, count.getAndIncrement() ) );
        thread.setDaemon( true );
        return thread;
    }

}
