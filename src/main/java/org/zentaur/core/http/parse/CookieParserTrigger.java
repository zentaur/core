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

import static org.zentaur.core.http.parse.ParserStatus.COOKIE_NAME;
import static org.zentaur.core.http.parse.ParserStatus.COOKIE_VALUE;

import org.zentaur.core.http.CookieBuilder;
import org.zentaur.core.http.MutableRequest;
import org.zentaur.core.http.RequestParseException;

final class CookieParserTrigger
    implements ParserTrigger
{

    private CookieBuilder cookieBuilder;

    @Override
    public ParserStatus onToken( ParserStatus status, String token, MutableRequest request )
        throws RequestParseException
    {
        if ( COOKIE_NAME == status )
        {
            cookieBuilder = new CookieBuilder().setName( token );
            return COOKIE_VALUE;
        }

        request.addCookie( cookieBuilder.setValue( token ).build() );
        return COOKIE_NAME;
    }

}
