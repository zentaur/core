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

import static org.zentaur.core.io.IOUtils.utf8URLDecode;

import org.zentaur.core.http.MutableRequest;
import org.zentaur.core.http.RequestParseException;

abstract class AbstractParametersParserTrigger
    implements ParserTrigger
{

    private final ParserStatus paramNameStatus;

    private final ParserStatus paramValueStatus;

    public AbstractParametersParserTrigger( ParserStatus paramNameStatus, ParserStatus paramValueStatus )
    {
        this.paramNameStatus = paramNameStatus;
        this.paramValueStatus = paramValueStatus;
    }

    private String namePtr;

    @Override
    public ParserStatus onToken( ParserStatus status, String token, MutableRequest request )
        throws RequestParseException
    {
        if ( paramNameStatus == status )
        {
            namePtr = token;
            return paramValueStatus;
        }
        onParameterFound( utf8URLDecode( namePtr ), utf8URLDecode( token ), request );
        return paramNameStatus;
    }

    protected abstract void onParameterFound( String name, String value, MutableRequest request );

}
