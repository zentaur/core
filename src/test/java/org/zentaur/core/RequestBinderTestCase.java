package org.zentaur.core;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zentaur.http.Request;
import org.zentaur.http.RequestHandler;
import org.zentaur.http.Response;

public final class RequestBinderTestCase
{

    private RequestDispatcher dispatcher;

    private RequestHandler mock1;

    private RequestHandler mock2;

    @Before
    public void setUp()
    {
        mock1 = mock( RequestHandler.class );
        mock2 = mock( RequestHandler.class );

        dispatcher = new RequestDispatcher();
        dispatcher.addRequestHandler( "/mock1", mock1 );
        dispatcher.addRequestHandler( "/mock2*", mock2 );
    }

    @After
    public void tearDown()
    {
        mock1 = null;
        mock2 = null;
        dispatcher = null;
    }

    @Test
    public void firstOneMatches()
        throws Exception
    {
        Request request = newMockRequest( "/mock1" );
        Response response = mock( Response.class );

        dispatcher.dispatch( request, response );

        verify( mock1, times( 1 ) ).handle( request, response );
        verify( mock2, never() ).handle( request, response );
    }

    @Test
    public void secondOneMatches()
        throws Exception
    {
        Request request = newMockRequest( "/mock2" );
        Response response = mock( Response.class );

        dispatcher.dispatch( request, response );

        verify( mock1, never() ).handle( request, response );
        verify( mock2, times( 1 ) ).handle( request, response );
    }

    @Test
    public void secondOneMatchesWithLongerPattern()
        throws Exception
    {
        Request request = newMockRequest( "/mock2/some/extra/path.jsp" );
        Response response = mock( Response.class );

        dispatcher.dispatch( request, response );

        verify( mock1, never() ).handle( request, response );
        verify( mock2, times( 1 ) ).handle( request, response );
    }

    @Test
    public void noOneMatches()
        throws Exception
    {
        Request request = newMockRequest( "/mock3" );
        Response response = mock( Response.class );

        dispatcher.dispatch( request, response );

        verify( mock1, never() ).handle( request, response );
        verify( mock2, never() ).handle( request, response );
    }

    private static Request newMockRequest( String path )
    {
        final Request request = mock( Request.class );
        when( request.getPath() ).thenReturn( path );
        return request;
    }

}
