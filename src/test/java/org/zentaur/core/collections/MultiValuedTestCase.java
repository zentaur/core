package org.zentaur.core.collections;

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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class MultiValuedTestCase
{

    private SimpleMultiValued<String, String> multiValued;

    @Before
    public void setUp()
    {
        multiValued = new SimpleMultiValued<String, String>()
                          .addValue( "Accept-Charset", "ISO-8859-1" )
                          .addValue( "Accept-Charset", "utf-8;q=0.7" )
                          .addValue( "Accept-Charset", "*;q=0.7" );
    }

    @After
    public void tearDown()
    {
        multiValued = null;
    }

    @Test( expected = UnsupportedOperationException.class )
    public void cannotOverrideValuesDirectly()
    {
        List<String> values = multiValued.getValues( "Accept-Charset" );
        values.add( "user preferred value" );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void cannotRemoveElementsFromIterator()
    {
        multiValued.getAllEntries().iterator().remove();
    }

    @Test( expected = UnsupportedOperationException.class )
    public void cannotOverrideEntryValue()
    {
        multiValued.getAllEntries().iterator().next().setValue( new ArrayList<String>() );
    }

}
