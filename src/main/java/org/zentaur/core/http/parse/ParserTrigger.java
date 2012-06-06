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

import org.zentaur.core.http.MutableRequest;
import org.zentaur.core.http.RequestParseException;

/**
 * ParserTrigger instances are invoked depending on the {@link ParserStatus}.
 */
interface ParserTrigger
{

    /**
     * Performs an parse action on the input token, adding data to the request, depending on the parser status.
     *
     * @param status the current parser status.
     * @param token the consumed token.
     * @param request the request that the parser is currently building
     * @throws RequestParseException if any syntax error occurs
     */
    ParserStatus onToken( ParserStatus status, String token, MutableRequest request )
        throws RequestParseException;

}
