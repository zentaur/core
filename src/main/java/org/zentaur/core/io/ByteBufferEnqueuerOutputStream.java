package org.zentaur.core.io;

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

import static java.nio.ByteBuffer.allocate;
import static org.zentaur.lang.Preconditions.checkArgument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Queue;

/**
 * A special {@link OutputStream} instances that, while writing bytes,
 * creates chunks of fixed size that will be enqueued in the given queue;
 * moreover, it takes the count of written bytes.
 */
public final class ByteBufferEnqueuerOutputStream
    extends OutputStream
{

    public static final ByteBuffer EOM = allocate( 0 );

    private static final int DEFAULT_BUFFER_CHUNK_SIZE = 1024;

    private final Queue<ByteBuffer> buffers;

    private ByteBuffer currentPtr;

    private final int chunkSize;

    private long writtenBytes = 0;

    /**
     * Creates a new {@code ByteBufferEnqueuerOutputStream} instance.
     *
     * @param buffers the queue where storing the chunks.
     */
    public ByteBufferEnqueuerOutputStream( Queue<ByteBuffer> buffers )
    {
        this( buffers, DEFAULT_BUFFER_CHUNK_SIZE );
    }

    public ByteBufferEnqueuerOutputStream( Queue<ByteBuffer> buffers, int chunkSize )
    {
        checkArgument( buffers != null, "Impossible to send data to a null ByteBuffer queue" );
        checkArgument( chunkSize > 0, "chunk size must be a positive integer" );
        this.buffers = buffers;
        this.chunkSize = chunkSize;

        newChunk();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write( int b )
        throws IOException
    {
        if ( !currentPtr.hasRemaining() )
        {
            flush();
        }

        writtenBytes++;
        currentPtr.put( (byte) ( b & 0xFF ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush()
        throws IOException
    {
        if ( currentPtr.position() == 0 )
        {
            return;
        }

        // resize the Buffer to discard extra bytes
        if ( currentPtr.position() < currentPtr.limit() )
        {
            currentPtr.limit( currentPtr.position() );
        }
        currentPtr.rewind();
        buffers.offer( currentPtr );

        newChunk();
    }

    /**
     * Allocates a new ByteBuffer pointer.
     */
    private void newChunk()
    {
        currentPtr = allocate( chunkSize );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close()
        throws IOException
    {
        buffers.offer( EOM );
    }

    /**
     * Returns the number of written bytes.
     *
     * @return the number of written bytes.
     */
    public long getWrittenBytes()
    {
        return writtenBytes;
    }

}
