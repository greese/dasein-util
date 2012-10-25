/**
 * Copyright (C) 1998-2012 enStratus Networks Inc
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.dasein.util;

import org.dasein.util.uom.time.Second;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Verifies the functioning of the jiterator of code.
 * @author George Reese
 * @since 2012.02
 * @version 2012.02
 */
public class CursorTestCase {
    @Rule
    public TestName testName = new TestName();

    public CursorTestCase() { }

    public String getName() {
        return testName.getMethodName();
    }
    
    @Test
    public void testSimpleCursor() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", null) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                for( String s : source ) {
                    cursor.push(s);
                    try { Thread.sleep(1000L); }
                    catch( InterruptedException ignore ) { }
                }
            }
        };
        
        populator.populate();
        ForwardCursor<String> result = populator.getCursor();
        int count = 0;
        
        for( String s : result ) {
            String original = source.get(count++);
            
            assertEquals("Element " + (count-1) + " differs", original, s);
        }
        assertEquals("Count of elements is not right", source.size(), count);
    }

    static private final Random random = new Random();
    
    @Test
    public void testStaggerdCursor() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", null) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                for( String s : source ) {
                    cursor.push(s);
                    
                    if( random.nextBoolean() && random.nextBoolean() ) {
                        try { Thread.sleep(1000L); }
                        catch( InterruptedException ignore ) { }
                    }
                }
            }
        };

        populator.populate();
        ForwardCursor<String> result = populator.getCursor();
        int count = 0;

        for( String s : result ) {
            String original = source.get(count++);

            assertEquals("Element " + (count-1) + " differs", original, s);
        }
        assertEquals("Count of elements is not right", source.size(), count);
    }
    
    @Test
    public void testCursorExceptionAtBeginning() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", null) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                throw new RuntimeException("This is thrown at the beginning");
            }
        };
        populator.populate();
        Iterable<String>  result = populator.getCursor();
        
        try {
            //noinspection UnusedDeclaration
            for( String s : result ) {
                
            }
            fail("List finished without an exception");
        }
        catch( RuntimeException e ) {
            // success
        }
    }

    @Test
    public void testCursorExceptionInMiddle() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", null) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                int i = 0;

                for( String s : source ) {
                    cursor.push(s);
                    if( (i++) == 5 ) {
                        throw new RuntimeException("Middle exception");
                    }
                }
            }
        };
        populator.populate();
        Iterable<String>  result = populator.getCursor();

        try {
            //noinspection UnusedDeclaration
            for( String s : result ) {

            }
            fail("List finished without an exception");
        }
        catch( RuntimeException e ) {
            // success
        }
    }

    @Test
    public void testCursorExceptionAtEnd() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", null) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                int i = 0;

                for( String s : source ) {
                    cursor.push(s);
                }
                throw new RuntimeException("End exception");
            }
        };
        populator.populate();
        Iterable<String>  result = populator.getCursor();

        try {
            //noinspection UnusedDeclaration
            for( String s : result ) {

            }
            fail("List finished without an exception");
        }
        catch( RuntimeException e ) {
            // success
        }
    }

    @Test
    public void testCursorTimeout() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", new org.dasein.util.uom.time.TimePeriod<Second>(10, org.dasein.util.uom.time.TimePeriod.SECOND)) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                for( String s : source ) {
                    cursor.push(s);
                    try { Thread.sleep(60000L); }
                    catch( InterruptedException ignore ) { }
                }
            }
        };

        populator.populate();
        Iterable<String> result = populator.getCursor();

        try {
            //noinspection UnusedDeclaration
            for( String s : result ) {
                System.out.println((new Date()) + ": " + s);
            }
            fail("List finished without an exception");
        }
        catch( RuntimeException e ) {
            //noinspection AssertEqualsBetweenInconvertibleTypes
            assertEquals("Not the right exception", JiteratorLoadException.class, e.getClass());
            Throwable t = e.getCause();
            while( t.getCause() != null ) {
                t = t.getCause();
            }
            assertEquals("Not the right exception", TimeoutException.class, t.getClass());
        }
    }

    @Test
    public void testCursorWithNull() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
            source.add(null);
        }
        CursorPopulator<String> populator = new CursorPopulator<String>("Test Cursor", null) {
            @Override
            public void populate(ForwardCursor<String> cursor) {
                for( String s : source ) {
                    cursor.push(s);
                    try { Thread.sleep(1000L); }
                    catch( InterruptedException ignore ) { }
                }
            }
        };

        populator.populate();
        ForwardCursor<String> result = populator.getCursor();
        int count = 0;

        for( String s : result ) {
            String original = source.get(count++);

            assertEquals("Element " + (count-1) + " differs", original, s);
        }
        assertEquals("Count of elements is not right", source.size(), count);
    }
}
