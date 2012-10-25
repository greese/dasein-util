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

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.dasein.util.uom.time.*;
import org.dasein.util.uom.time.TimePeriod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PopulatorThread<T> implements Runnable {
    static private final Logger logger = Logger.getLogger(PopulatorThread.class);
    
    static private final ExecutorService threadPool = Executors.newCachedThreadPool();
    
    static {
    	try {
    		PoolTerminator.addTerminationHandler(new Callable<Boolean>() {
    			public Boolean call() {
    				threadPool.shutdown();
    				return true;
    			}
    		});
    	}
    	catch( Throwable ignore ) {
    		// this will get thrown when not in a J2EE container
    	}
    }

    @SuppressWarnings("unused")
    static public void terminate() {
    	threadPool.shutdown();
    }
    
    private JitCollection<T>                                 collection;
    private Jiterator<T>                                     iterator;
    private JiteratorPopulator<T>                            populator;
    private org.dasein.util.uom.time.TimePeriod<Millisecond> timeout;
    
    public PopulatorThread(@Nonnull JiteratorPopulator<T> populator) {
        this(null, populator, null);
    }
    
    public PopulatorThread(@Nullable org.dasein.util.uom.time.TimePeriod<?> timeout, @Nonnull JiteratorPopulator<T> populator) {
        this(timeout, populator, null);
    }
    
    public PopulatorThread(@Nullable org.dasein.util.uom.time.TimePeriod<?> timeout, @Nonnull JiteratorPopulator<T> populator, @Nullable JiteratorFilter<T> filter) {
        this.populator = populator;
        if( timeout != null ) {
            this.timeout = (org.dasein.util.uom.time.TimePeriod<Millisecond>)timeout.convertTo(TimePeriod.MILLISECOND);
        }
        iterator = new Jiterator<T>(null, null, filter, this.timeout);
        collection = new JitCollection<T>(iterator, "Jiterator Collection Loader");
    }
    
    public @Nonnull Collection<T> getResult() {
        return collection;
    }
    
    public void populate() {
        threadPool.submit(this);
    }

    public void setSize(int size) {
        collection.setSize(size);
    }

    public void run() {
        boolean success = false;
        
        try {
            Thread.currentThread().setName("Jiterator Populator");
            populator.populate(iterator);
            success = true;
        }
        catch( Exception e ) {
            iterator.setLoadException(e);
        }
        catch( Throwable t ) {
            iterator.setLoadException(new RuntimeException(t));
        }
        finally {
            if( success ) {
                iterator.complete();
            }
        }
    }
}
