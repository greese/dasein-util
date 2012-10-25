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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Verifies the functioning of the jiterator of code.
 * @author George Reese
 * @since 2012.02
 * @version 2012.02
 */
public class JiteratorTestCase {
    @Rule
    public TestName testName = new TestName();

    public JiteratorTestCase() { }

    public String getName() {
        return testName.getMethodName();
    }
    
    @Test
    public void testSimpleJiterator() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(new JiteratorPopulator<String>() {
            @Override
            public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                for( String s : source ) {
                    iterator.push(s);
                    try { Thread.sleep(1000L); }
                    catch( InterruptedException ignore ) { }
                }
            }
        });
        populator.populate();
        Collection<String>  result = populator.getResult();
        
        assertEquals("Not the same size", source.size(), result.size());
        int i=0;
        
        for( String s : result ) {
            String original = source.get(i++);
            
            assertEquals("Element " + (i-1) + " differs", original, s);
        }
    }

    @Test
    public void testJiteratorExceptionAtBeginning() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(new JiteratorPopulator<String>() {
            @Override
            public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                throw new RuntimeException("This is thrown at the beginning");
            }
        });
        populator.populate();
        Collection<String>  result = populator.getResult();
        
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
    public void testJiteratorExceptionInMiddle() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(new JiteratorPopulator<String>() {
            @Override
            public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                int i = 0;
                
                for( String s : source ) {
                    iterator.push(s);
                    if( (i++) == 5 ) {
                        throw new RuntimeException("Middle exception");
                    }
                }
            }
        });
        populator.populate();
        Collection<String>  result = populator.getResult();

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
    public void testJiteratorExceptionAtEnd() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(new JiteratorPopulator<String>() {
            @Override
            public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                for( String s : source ) {
                    iterator.push(s);
                }
                throw new RuntimeException("End exception");
            }
        });
        populator.populate();
        Collection<String>  result = populator.getResult();

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
    public void testJiteratorTimeout() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(
                new org.dasein.util.uom.time.TimePeriod<Second>(10, org.dasein.util.uom.time.TimePeriod.SECOND),
                new JiteratorPopulator<String>() {
                    @Override
                    public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                        for( String s : source ) {
                            try { Thread.sleep(60000L); }
                            catch( InterruptedException ignore ) { }
                            iterator.push(s);
                        }
                    }
               }
        );
        populator.populate();
        Collection<String>  result = populator.getResult();

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
    public void testJiteratorWithNullValues() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
            source.add(null);
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(new JiteratorPopulator<String>() {
            @Override
            public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                for( String s : source ) {
                    iterator.push(s);
                    try { Thread.sleep(1000L); }
                    catch( InterruptedException ignore ) { }
                }
            }
        });
        populator.populate();
        Collection<String>  result = populator.getResult();

        assertEquals("Not the same size", source.size(), result.size());
        int i=0;

        for( String s : result ) {
            String original = source.get(i++);

            assertEquals("Element " + (i-1) + " differs", original, s);
        }
    }

    @Test
    public void testJiteratorFilterNullValues() {
        final ArrayList<String> source = new ArrayList<String>();

        for( int i=0; i< 10; i++ ) {
            source.add(String.valueOf(i));
        }
        PopulatorThread<String> populator = new PopulatorThread<String>(
                null,
                new JiteratorPopulator<String>() {
                    @Override
                    public void populate(@Nonnull Jiterator<String> iterator) throws Exception {
                        for( String s : source ) {
                            iterator.push(s);
                            iterator.push(null);
                            try { Thread.sleep(1000L); }
                            catch( InterruptedException ignore ) { }
                        }
                    }
                },
                new JiteratorFilter<String>() {
                    @Override
                    public boolean filter(@Nullable String item) throws Throwable {
                        return (item != null);
                    }
                }
        );
        populator.populate();
        Collection<String>  result = populator.getResult();

        assertEquals("Not the same size", source.size(), result.size());
        int i=0;

        for( String s : result ) {
            String original = source.get(i++);

            assertEquals("Element " + (i-1) + " differs", original, s);
        }
    }
}
