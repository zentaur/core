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

import static org.zentaur.core.http.parse.ParserStatus.PATH;
import static org.zentaur.http.Request.Method.valueOf;
import static org.zentaur.http.Request.Method.values;

import java.util.Arrays;

import org.zentaur.core.http.MutableRequest;
import org.zentaur.core.http.RequestParseException;

/**
 * Trigger for the HTTP request method.
 */
final class MethodParserTrigger
    implements ParserTrigger
{

    /**
     * {@inheritDoc}
     */
    @Override
    public ParserStatus onToken( ParserStatus status, String token, MutableRequest request )
        throws RequestParseException
    {
        try
        {
            request.setMethod( valueOf( token ) );
            return PATH;
        }
        catch ( IllegalArgumentException e )
        {
            throw new RequestParseException( "Custom method '%s' is not supported, only %s supported",
                                             token, Arrays.toString( values() ) );
        }
    }

}
