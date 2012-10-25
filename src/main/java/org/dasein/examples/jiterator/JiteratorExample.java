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

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Shows how to use the "just-in-time" iterators that enable you to consume iterable collections before the
 * entire collection is available. A typical use case might be loading a large data set from a database or
 * web service where you want the items being shown in your user interface as soon as they become available.
 * @author George Reese (george.reese@imaginary.com)
 */
public class JiteratorExample {

    static public void main(@Nonnull String ... args) {
        DummyDatabaseLoader loader = new DummyDatabaseLoader();
        
        // Query the database.
        // Qe will get a result back immediately... likely before the query even goes to the database.
        Collection<DummyDatabaseObject> results = loader.findAll();
        
        // Loop through the results.
        // We will see the first object well before the full data set is loaded
        for( DummyDatabaseObject object : results ) {
            System.out.println("Reading: " + object.objectId + " (" + object.name + ")");
        }
        System.out.println("Done (this process will take a minute to exit)");
    }
}
