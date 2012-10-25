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

package org.dasein.examples.jiterator;

import org.dasein.util.Jiterator;
import org.dasein.util.JiteratorPopulator;
import org.dasein.util.PopulatorThread;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Pretends to load strings from a database. It doesn't really talk to a database, but it loads slowly to demonstrate
 * the time it takes to load a large data set from a database or web service. Practically, it shows how
 * one loads a jiterator for consumption by another thread.
 * @author George Reese (george.reese@enstratus.com)
 */
public class DummyDatabaseLoader {
    
    public Collection<DummyDatabaseObject> findAll() {
        // Construct a jiterator populator to perform the actual load of objects asynchronously.
        PopulatorThread<DummyDatabaseObject> populator = new PopulatorThread<DummyDatabaseObject>(new JiteratorPopulator<DummyDatabaseObject>() {
            @Override
            public void populate(@Nonnull Jiterator<DummyDatabaseObject> iterator) throws Exception {
                // Load the data from the database
                query(iterator);
            }
        });
        
        // Run the populator. This returns immediately.
        populator.populate();
        
        // Return the result (an instance of JitCollection<DummyDatabaseObject>) as a generic collection.
        // Note that nothing is likely loaded at this point.
        // The generic collection hides the fact that it is empty from anything consuming it.
        // If the consumer requests a value that isn't yet loaded, it will block at that point.
        return populator.getResult();
    }
    
    private void query(Jiterator<DummyDatabaseObject> iterator) throws Exception {
        // Note that if we throw an exception here, it will be sent up to any attempt to fetch data from
        // the JitCollection created earlier.
        for( int i=0; i<100; i++ ) {
            DummyDatabaseObject dummy = new DummyDatabaseObject();
            
            dummy.objectId = i;
            dummy.name = "Object " + i;
            System.out.println("Pushing: " + dummy.objectId);
            iterator.push(dummy);
            // Put in a sleep here to mimic the latency of loading from a remote source.
            try { Thread.sleep(200L); }
            catch( InterruptedException ignore ) { }
        }
        System.out.println("LOAD COMPLETE");
    }
}

