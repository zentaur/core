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

import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.nio.charset.Charset.forName;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Utility class for I/O management.
 */
public final class IOUtils
{

    private static final Charset UTF_8 = forName( "UTF-8" );

    /**
     * Hidden constructor, this class must not be instantiated.
     */
    private IOUtils()
    {
        // do nothing
    }

    /**
     * Builds a message replacing placeholders in the input template
     * and encodes the result.
     *
     * @param messageTemplate a format string
     * @param args Arguments referenced by the format specifiers in the format string
     * @return the UTF-8 encoded formatted ByteBuffer
     * @see String#format(String, Object...)
     */
    public static ByteBuffer utf8ByteBuffer( String messageTemplate, Object...args )
    {
        return UTF_8.encode( format( messageTemplate, args ) );
    }

    /**
     * Converts the input {@link ByteBuffer} in an UTF-8 {@link CharBuffer}.
     *
     * @param buffer the buffer has to be converted.
     * @return the UTF-8 converted buffer.
     */
    public static CharBuffer toUtf8CharBuffer( ByteBuffer buffer )
    {
        return UTF_8.decode( buffer );
    }

    /**
     * Converts the input {@link ByteBuffer} in an UTF-8 String
     *
     * @param buffer the buffer has to be converted.
     * @return the UTF-8 converted string.
     */
    public static String toUtf8String( ByteBuffer buffer )
    {
        return toUtf8CharBuffer( buffer ).toString();
    }

    /**
     * Decodes the URL encoded input string, in UTF-8.
     *
     * @param input the string has to be decoded.
     * @return the decoded version of input string.
     */
    public static String utf8URLDecode( String input )
    {
        try
        {
            return decode( input, UTF_8.displayName() );
        }
        catch ( UnsupportedEncodingException e )
        {
            // should not happen
            return "";
        }
    }

    /**
     * Close quietly the input closeable.
     *
     * @param closeable the closeable instance has to be closed.
     */
    public static void closeQuietly( Closeable closeable )
    {
        if ( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch ( IOException e )
            {
                // swallow
            }
        }
    }

}
