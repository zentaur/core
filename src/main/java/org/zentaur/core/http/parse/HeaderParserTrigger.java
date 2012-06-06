package org.zentaur.core.http.parse;

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

import static java.lang.Long.parseLong;
import static org.zentaur.core.http.parse.ParserStatus.COOKIE_NAME;
import static org.zentaur.core.http.parse.ParserStatus.HEADER_NAME;
import static org.zentaur.core.http.parse.ParserStatus.HEADER_USER_AGENT_VALUE;
import static org.zentaur.core.http.parse.ParserStatus.HEADER_VALUE;
import static org.zentaur.http.Headers.CONTENT_LENGTH;
import static org.zentaur.http.Headers.COOKIE;
import static org.zentaur.http.Headers.USER_AGENT;

import org.zentaur.core.http.MutableRequest;
import org.zentaur.core.http.RequestParseException;

final class HeaderParserTrigger
    implements ParserTrigger
{

    private String headerNamePtr;

    @Override
    public ParserStatus onToken( ParserStatus status, String token, MutableRequest request )
        throws RequestParseException
    {
        if ( HEADER_NAME == status )
        {
            headerNamePtr = token;

            if ( USER_AGENT.equals( headerNamePtr ) )
            {
                return HEADER_USER_AGENT_VALUE;
            }
            else if ( COOKIE.equals( headerNamePtr ) )
            {
                headerNamePtr = null; // no longer needed
                return COOKIE_NAME; // switch to cookie trigger
            }
        }
        else
        {
            request.addHeader( headerNamePtr, token );

            if ( CONTENT_LENGTH.equals( headerNamePtr ) )
            {
                try
                {
                    request.setContentLength( parseLong( token ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new RequestParseException( "{} header value {} is not a numeric format",
                                                     CONTENT_LENGTH, token );
                }
            }

            if ( HEADER_USER_AGENT_VALUE == status )
            {
                return HEADER_NAME;
            }
        }

        return HEADER_VALUE;
    }

}
