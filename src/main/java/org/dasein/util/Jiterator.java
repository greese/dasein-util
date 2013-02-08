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

import com.sun.istack.internal.Nullable;
import org.apache.log4j.Logger;
import org.dasein.util.uom.time.Millisecond;
import org.dasein.util.uom.time.TimePeriod;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * <p>
 * A "Just-in-time" iterator for streaming a flow of objects across an iterable interface.
 * This class is useful in situations where you don't want to keep your list of objects around
 * in memory, but instead are using the collection to move them from one location (often slow-loading)
 * to another.
 * </p>
 * <p>
 * To use the jiterator, just construct it and starting pushing items into it. Once the last item has
 * been added, call {@link #complete()}.
 * </p>
 * <p>
 * Last Modified $Date: 2009/07/02 01:37:02 $
 * </p>
 * @author George Reese
 * @param <T> the type of object being managed in the jiterator
 */
public class Jiterator<T> implements Iterator<T>, Iterable<T> {
    static private final Logger logger = Logger.getLogger(Jiterator.class);
    static private final Random idGenerator = new Random();

    private final JiteratorFilter<T> filter;
    private final String             jiteratorId;
    private volatile long            lastTouch;
    private volatile Exception       loadException;
    private boolean                  loaded;
    private final String             name;
    private TimePeriod<Millisecond>  timeout;
    private ArrayList<T>             waiting;

    private transient boolean nexting = false;
    
    /**
     * Constructs an empty jiterator into which you can start adding items.
     */
    public Jiterator() {
        this(null, null, null, null);
    }

    @SuppressWarnings("unused")
    public Jiterator(@Nullable JiteratorFilter<T> filter) {
        this(null, null, filter, null);
    }
    
    /**
     * Constructs a jiterator from the specified collection. This jiterator will load
     * items from the source collection in a background thread. It will <b>not</b>
     * make the jiterator complete. You can thus add elements in addition to the source
     * collection.
     * @param list the source list of items to initialize the jiterator with
     */
    @SuppressWarnings("unused")
    public Jiterator(@Nullable Collection<T> list) {
        this(null, list, null, null);
    }

    /**
     * Constructs a jiterator with the specified name (useful for debugging).
     * @param name the name of the jiterator
     */
    @SuppressWarnings("unused")
    public Jiterator(@Nullable String name) {
        this(name, null, null, null);
    }

    /**
     * Constructs a jiterator with the specified name and timeout.
     * @param name the name of the jiterator
     * @param timeout the timeout period after going without a touch
     */
    @SuppressWarnings("unused")
    public Jiterator(@Nullable String name, @Nullable org.dasein.util.uom.time.TimePeriod<?> timeout) {
        this(name, null, null, timeout);
    }

    private class JiteratorTask implements Runnable {
        private final Collection<T> flist;

        private JiteratorTask(Collection<T> flist) {
            this.flist = flist;
        }

        @Override
        public void run() {
            for( T item : this.flist ) {
                push(item);
            }
        }
    }
    
    /**
     * Constructs a jiterator with all default values set.
     * @param name the name of the jiterator (useful in debugging)
     * @param starterList a starter list of items to populate the list (populated asynchronously)
     * @param filter a filter to filter out items being added to the list
     * @param timeout a timeout period that will cause the jiterator to timeout with an error 
     */
    public Jiterator(@Nullable String name, @Nullable Collection<T> starterList, @Nullable JiteratorFilter<T> filter, @Nullable TimePeriod<?> timeout) {
        lastTouch = System.currentTimeMillis();
        loaded = false;
        waiting = new ArrayList<T>();
        jiteratorId = new UUID(idGenerator.nextLong(), idGenerator.nextLong()).toString();
        this.filter = filter;
        if( name != null ) {
            this.name = name;
        }
        else {
            this.name = Thread.currentThread().getName();
        }
        if( timeout != null ) {
            this.timeout = (TimePeriod<Millisecond>)timeout.convertTo(TimePeriod.MILLISECOND);
        }
        else {
            this.timeout = new TimePeriod<Millisecond>(CalendarWrapper.MINUTE * 10L, TimePeriod.MILLISECOND);
        }
        if( starterList != null ) {
            DaseinUtilTasks.submit(new JiteratorTask(starterList));
        }
    }
    /**
     * Marks the jiterator as complete. Once you call this method, you cannot add any more
     * items into the jiterator. If you fail to call this method, any threads reading from this
     * jiterator will ultimately hang until you call this method.
     */
    public synchronized void complete() {
        if( logger.isInfoEnabled() ) {
            logger.info("[" + this + "] Marking complete");
        }
        if( loaded ) {
            return;
        }
        loaded = true;
        lastTouch = System.currentTimeMillis();
        if( waiting != null && waiting.isEmpty() ) {
            waiting = null;
        }
        notifyAll();
    }

    /**
     * @return a unique ID for this jiterator to help in debugging
     */
    public @Nonnull String getJiteratorId() {
        return jiteratorId;
    }
            
    /**
     * Provides a user-friendly name for the loader
     * @return the user-friendly name for the loader
     */
    public @Nonnull String getName() {
        return name;
    }
    
    /**
     * Checks to see if there are more elements to be processed in the jiterator. If this method
     * is called prior to the jiterator being loaded with an item, it will hang until
     * either an item is added into the jiterator or the jiterator is marked as complete.
     * @return true if there are more elements to be processed
     * @throws JiteratorLoadException an error occurred during the load of the jiterator
     */
    public synchronized boolean hasNext() {
        while( nexting ) {
            if( logger.isInfoEnabled() ) {
                logger.info("[" + this + "] Waiting for another thread to pull item...");
            }
            try { wait(150L); }
            catch( InterruptedException e ) { /* ignore */ }
        }
        if( loadException != null ) {
            throw new JiteratorLoadException(loadException);
        }
        nexting = true;
        try {
            if( waiting == null ) {
                return false;
            }
            if( !waiting.isEmpty() ) {
                return true;
            }
            waitForPush();
            if( loadException != null ) {
                throw new JiteratorLoadException(loadException);
            }
            return (waiting != null && !waiting.isEmpty());
        }
        finally {
            nexting = false;
            notifyAll();
        }
    }

    /** 
     * @return true if all items have been processed and this jiterator can be discarded
     */
    @SuppressWarnings("unused")
    public synchronized boolean isComplete() {
        return (waiting == null);
    }
    
    /**
     * @return true if all items have been loaded into the jiterator
     */
    @SuppressWarnings("unused")
    public synchronized boolean isLoaded() {
        return loaded;
    }

    /**
     * @return this jiterator
     */
    public @Nonnull Iterator<T> iterator() {
        if( loadException != null ) {
            throw new JiteratorLoadException(loadException);
        }
        return this;
    }
    
    /**
     * Provides the next element in the jiterator. If this method
     * is called prior to the jiterator being loaded with an item, it will hang until
     * either an item is added into the jiterator or the jiterator is marked as complete.
     * @return true if there are more elements to be processed
     * @throws NoSuchElementException an attempt was made to read beyond the last item in the jiterator
     */    
    public synchronized @Nullable T next() throws JiteratorLoadException {
        while( nexting ) {
            if( logger.isInfoEnabled() ) {
                logger.info("[" + this + "] Waiting for another thread to pull item...");
            }
            try { wait(150L); }
            catch( InterruptedException e ) { /* ignore */ }
        }
        if( loadException != null ) {
            throw new JiteratorLoadException(loadException);
        }
        nexting = true;
        try {
            T t;
            
            if( waiting == null ) {
                throw new NoSuchElementException("Invalid attempt to get another element from empty iterator.");
            }
            if( !waiting.isEmpty() ) {
                t = waiting.get(0);
                waiting.remove(0);
                if( waiting.isEmpty() && loaded ) { 
                    waiting = null;
                } 
                return t;
            }
            waitForPush();
            if( loadException != null ) {
                throw new JiteratorLoadException(loadException);
            }
            if( waiting == null || waiting.isEmpty() ) {
                throw new NoSuchElementException("Invalid attempt to get another element from empty iterator.");            
            }
            t = waiting.get(0);
            waiting.remove(0);
            if( waiting.isEmpty() && loaded ) { 
                waiting = null;
            }
            return t;        
        }
        finally {
            nexting = false;
            notifyAll();
        }
    }

    /**
     * Pushes a new item into the jiterator. The new item will be added to the end of the jiterator.
     * @param item the item to be added
     * @throws IllegalStateException an attempt was made to push an item on a jiterator marked complete
     */
    public synchronized void push(@Nullable T item) {
        if( waiting == null ) {
            throw new IllegalStateException("Invalid attempt to add an item to a completed list.");
        }
        if( filter != null ) {
            try {
                if( !filter.filter(item) ) {
                    return;
                }
            }
            catch( Throwable t ) {
                logger.error("[" + this + "] Error filtering " + item + ": " + t.getMessage());
                Exception e;
                
                if( t instanceof Exception ) {
                    e = (Exception)t;
                }
                else {
                    e = new RuntimeException(t);
                }
                setLoadException(e);
                return;
            }
        }
        waiting.add(item);
        lastTouch = System.currentTimeMillis();
        notifyAll();
    }
    
    /**
     * This operation is not allowed
     * @throws UnsupportedOperationException always thrown
     */
    public synchronized void remove() {
        throw new UnsupportedOperationException("Removing from a jiterator is not supported.");
    }

    public synchronized void setLoadException(Exception e) {
        logger.warn("[" + this + "] Setting error for jiterator " + this + ": " + e.getMessage());
        loadException  = e;
        loaded = true;
        waiting = null;
        nexting = false;
        lastTouch = System.currentTimeMillis();
        notifyAll();
    }
    
    private transient long scream = 0L;
    /**
     * Waits for a new item to be put into the jiterator or for the jiterator to be marked empty.
     */
    private synchronized void waitForPush() {
        long waitStart = -1L;
        
        while( waiting != null && waiting.isEmpty() && !loaded ) {
            long untouched = System.currentTimeMillis() - lastTouch;
            
            if( untouched > timeout.longValue() ) {
                logger.error("[" + this + "] Jiterator timeout for " + getName());
                setLoadException(new TimeoutException("Jiterator " + getName() + " timed out while loading"));
            }
            if( waitStart == -1L ) {
                waitStart = System.currentTimeMillis();
            }
            else {
                if( (System.currentTimeMillis() - waitStart) > CalendarWrapper.MINUTE ) {
                    if( (System.currentTimeMillis() - scream) > CalendarWrapper.MINUTE ) {
                        scream = System.currentTimeMillis();
                        logger.warn("[" + this + "] " + ((System.currentTimeMillis()-lastTouch)/1000) + " seconds since last touch.");
                    }
                }
            }
            long multiplier = (untouched/(15 * CalendarWrapper.SECOND)) + 1;

            try { wait(150L * multiplier); }
            catch( InterruptedException ignore ) { }
        }        
    }
    
    @Override
    public String toString() {
        return (getName() + " [#" + getJiteratorId() + "] - (" + (new Date(lastTouch)) + ")");
    }
}
