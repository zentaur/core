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

import static org.zentaur.core.http.parse.ParserStatus.QS_PARAM_NAME;
import static org.zentaur.core.http.parse.ParserStatus.QS_PARAM_VALUE;

import org.zentaur.core.http.MutableRequest;

final class QueryStringParametersParserTrigger
    extends AbstractParametersParserTrigger
{

    public QueryStringParametersParserTrigger()
    {
        super( QS_PARAM_NAME, QS_PARAM_VALUE );
    }

    @Override
    protected void onParameterFound( String name, String value, MutableRequest request )
    {
        request.addQueryStringParameter( name, value );
    }

}
