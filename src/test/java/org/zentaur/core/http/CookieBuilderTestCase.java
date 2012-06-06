package org.zentaur.core.http;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zentaur.http.Cookie;

public final class CookieBuilderTestCase
{

    private CookieBuilder cookieBuilder;

    @Before
    public void setUp()
    {
        cookieBuilder = new CookieBuilder();
    }

    @After
    public void tearDown()
    {
        cookieBuilder = null;
    }

    // domain

    @Test( expected = IllegalArgumentException.class )
    public void nullDomainNotAllowed()
    {
        cookieBuilder.setDomain( null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void emptyDomainNotAllowed()
    {
        cookieBuilder.setDomain( "" );
    }

    // name

    @Test( expected = IllegalArgumentException.class )
    public void nullNameNotAllowed()
    {
        cookieBuilder.setName( null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void emptyNameNotAllowed()
    {
        cookieBuilder.setName( "" );
    }

    // value

    @Test( expected = IllegalArgumentException.class )
    public void nullValueNotAllowed()
    {
        cookieBuilder.setValue( null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void emptyValueNotAllowed()
    {
        cookieBuilder.setValue( "" );
    }

    // path

    @Test( expected = IllegalArgumentException.class )
    public void nullPathNotAllowed()
    {
        cookieBuilder.setPath( null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void emptyPathNotAllowed()
    {
        cookieBuilder.setPath( "" );
    }

    // max age

    @Test( expected = IllegalArgumentException.class )
    public void negativeMaxAgeNotAllowedExceptMinusOne()
    {
        cookieBuilder.setMaxAge( -10 );
    }

    // port

    @Test( expected = IllegalArgumentException.class )
    public void negativePortOrZeroNotAllowed()
    {
        cookieBuilder.addPort( 0 );
    }

    // complete cookie

    @Test
    public void verifyBuiltCookie()
    {
        Cookie cookie = cookieBuilder.setName( "made_write_conn" )
                                     .setValue( "Rg3vHJZnehYLjVg7qi3bZjzg" )
                                     .setDomain( ".foo.com" )
                                     .setPath( "/" )
                                     .setMaxAge( 30 )
                                     .addPort( 1234 )
                                     .build();

        assertEquals( "made_write_conn", cookie.getName() );
        assertEquals( "Rg3vHJZnehYLjVg7qi3bZjzg", cookie.getValue());
        assertEquals( ".foo.com", cookie.getDomain() );
        assertEquals( "/", cookie.getPath() );
        assertEquals( 30, cookie.getMaxAge() );
        assertTrue( cookie.getPorts().contains( 1234 ) );
    }

}
