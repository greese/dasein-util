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

import org.apache.log4j.Logger;
import org.dasein.util.uom.time.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * A single-direction cursor that stores in memory just the minimal amount of data required to enable
 * a consumer to move through an ordered set of items without the full data set needing to be loaded.
 * @param <T> the type of objects stored in the cursor
 */
public class ForwardCursor<T> implements Iterable<T> {
    Logger logger = Logger.getLogger(ForwardCursor.class);
    
    static private class CursorItem<T> {
        public Throwable loadError;
        public T item;
        public CursorItem<T> nextItem; 
    }

    private int                                              count;
    private CursorItem<T>                                    head;
    private String                                           name;
    private boolean                                          loaded;
    private int                                              size;
    private CursorItem<T>                                    tail;
    private org.dasein.util.uom.time.TimePeriod<Millisecond> timeout;
    
    public ForwardCursor(org.dasein.util.uom.time.TimePeriod<?> timeout) {
        this(null, timeout);
    }
    
    public ForwardCursor(String name, org.dasein.util.uom.time.TimePeriod<?> timeout) {
        count = 0;
        size = -1;
        if( timeout != null ) {
            this.timeout = (org.dasein.util.uom.time.TimePeriod<Millisecond>)timeout.convertTo(org.dasein.util.uom.time.TimePeriod.MILLISECOND);
        }
        else {
            this.timeout = new org.dasein.util.uom.time.TimePeriod<Millisecond>(CalendarWrapper.MINUTE * 10L, org.dasein.util.uom.time.TimePeriod.MILLISECOND);
        }
        if( name == null ) {
            this.name = UUID.randomUUID().toString();
        }
        else {
            this.name = name + " [" + UUID.randomUUID().toString() + "]";
        }
    } 
    
    public void complete() {
        synchronized( this ) {
            loaded = true;
            size = count;
            notifyAll();
        }
    }
    
    public void error(@Nonnull Throwable t) {
        synchronized( this ) {
            CursorItem<T> newTail = new CursorItem<T>();

            newTail.item = null;
            newTail.loadError = t;
            newTail.nextItem = null;
            if( head == null ) {
                head = newTail;
                tail = head;
            }
            else {
                tail.nextItem = newTail;
                tail = newTail;
            }
            loaded = true;
            notifyAll();
        }
    }
    
    public String getName() {
        return name;
    }

    public int getSize() {
        synchronized( this ) {
            return size;
        }
    }

    private boolean hasNextItem() {
        synchronized( this ) {
            waitForPush();
            if( head != null ) {
                if( head.loadError != null ) {
                    throw new JiteratorLoadException(head.loadError);
                }
                return true;
            }
            return false;
        }
    }
            
    @Override
    public @Nonnull Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return hasNextItem();
            }

            @Override
            public T next() {
                return nextItem();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove items from a forward cursor.");
            }
        };
    }
    
    private @Nullable T nextItem() {
        synchronized( this ) {
            waitForPush();
            if( head != null ) {
                CursorItem<T> current = head;

                head = head.nextItem;
                if( head == null ) {
                    tail = null;
                }

                if( current.loadError != null ) {
                    throw new JiteratorLoadException(current.loadError);
                }
                T item = current.item;

                // free up references
                current.nextItem = null;
                current.item = null;

                // return the value
                return item;
            }
            throw new ArrayIndexOutOfBoundsException("Attempt to move beyond the last item");
        }
    }
    
    public void push(@Nullable T item) {
        synchronized( this ) {
            lastTouch = System.currentTimeMillis();
            
            CursorItem<T> newTail = new CursorItem<T>();

            newTail.item = item;
            newTail.loadError = null;
            newTail.nextItem = null;
            if( head == null ) {
                head = newTail;
                tail = head;
            }
            else {
                tail.nextItem = newTail;
                tail = newTail;
            }
            count++;
            notifyAll();
        }
    }

    public void setSize(int size) {
        synchronized( this ) {
            if( this.size < 0 ) {
                this.size = size;
                notifyAll();
            }
        }
    }

    private transient long lastTouch = System.currentTimeMillis();
    private transient long scream = 0L;
    
    private void waitForPush() {
        long waitStart = -1L;

        while( head == null && !loaded ) {
            long untouched = System.currentTimeMillis() - lastTouch;

            if( untouched > timeout.longValue() ) {
                logger.error("[" + this + "] Cursor timeout for " + getName());
                this.error(new TimeoutException("Cursor timed out while loading"));
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
}
