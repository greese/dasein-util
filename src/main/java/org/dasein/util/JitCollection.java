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

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class JitCollection<T> implements Collection<T> {
    private volatile boolean   complete;
    private Throwable          error;
    final private List<T>      list;
    private int                size;
    final private Jiterator<T> source;
    private volatile List<T>   toAdd;

    @SuppressWarnings("unused")
    private JitCollection() {  throw new RuntimeException("Don't do this"); }
    
    @SuppressWarnings("unused")
    public JitCollection(@Nonnull Jiterator<T> src) {
        this(src, "Unknown");
    }

    public JitCollection(@Nonnull Jiterator<T> src, @Nonnull String nom) {
        source = src;
        size = -1;
        list  = new ArrayList<T>();
        DaseinUtilTasks.submit(new JitCollectionAddTask());
    }

    private class JitCollectionAddTask implements Runnable {
        @Override
        public void run() {
            try {
                for( T item : source ) {
                    synchronized( source ) {
                        if( complete ) {
                            return;
                        }
                        list.add(item);
                        source.notifyAll();
                    }
                }
            }
            catch( JiteratorLoadException e ) {
                synchronized( source ) {
                    error = e.getCause();
                    complete = true;
                    source.notifyAll();
                }
                toAdd = null;
                return;
            }
            catch( Throwable t ) {
                synchronized( source ) {
                    error = t;
                    complete = true;
                    source.notifyAll();
                }
                toAdd = null;
                return;
            }
            synchronized( source ) {
                if( toAdd != null ) {
                    list.addAll(toAdd);
                    toAdd = null;
                }
                complete = true;
                source.notifyAll();
            }
        }
    }

    @Override
    public boolean add(@Nullable T item) {
        synchronized( source ) {
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            if( complete ) {
                return list.add(item);
            }
            else {
                if( toAdd == null ) {
                    toAdd = new ArrayList<T>();
                }
                return toAdd.add(item);
            }
        }
    }

    public void add(@Nonnegative int index, @Nullable T element) {
        synchronized( source ) {
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            if( complete || list.size() > index ) {
                list.add(index, element);
            }
            else {
                while( !complete && list.size() <= index ) {
                    try { source.wait(100L); }
                    catch( InterruptedException e ) { /* ignore */ }
                }
                if( error != null ) {
                    throw new JiteratorLoadException(error);
                }
                list.add(index, element);
            }
        }
    }

    @Override
    public boolean addAll(@Nonnull Collection<? extends T> c) {
        synchronized( source ) {
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            if( complete) {
                return list.addAll(c);
            }
            else {
                if( toAdd == null ) {
                    toAdd = new ArrayList<T>();
                }
                return toAdd.addAll(c);
            }
        }
    }

    public boolean addAll(@Nonnegative int index, @Nonnull Collection<? extends T> c) {
        synchronized( source ) {
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            if( complete || list.size() > index ) {
                return list.addAll(index, c);
            }
            else {
                while( !complete && list.size() <= index ) {
                    try { wait(100L); }
                    catch( InterruptedException e ) { /* ignore */ }
                }
                if( error != null ) {
                    throw new JiteratorLoadException(error);
                }
                return list.addAll(index, c);
            }
        }        
    }

    @Override
    public void clear() {
        synchronized( source ) {
            error = null;
            complete = true;
            list.clear();
            toAdd = null;
        }
    }

    @Override
    public boolean contains(@Nullable Object item) {
        synchronized( source ) {
            while( !complete ) {
                if( list.contains(item) ) {
                    return true;
                }
                else if( toAdd != null && toAdd.contains(item) ) {
                    return true;
                }
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.contains(item);
        }
    }

    @Override
    public boolean containsAll(@Nonnull Collection<?> c) {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }                
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }            
            return list.containsAll(c);
        }
    }

    public T get(@Nonnegative int index) {
        synchronized( source ) {
            while( !complete && list.size() <= index ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }                                
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }            
            return list.get(index);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public int indexOf(@Nullable Object o) {
        synchronized( source ) {
            while( !complete ) {
                int idx;
                
                idx = list.indexOf(o);
                if( idx != -1 ) {
                    return idx;
                }
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }            
            return list.indexOf(o);
        }        
    }

    @Override
    public boolean isEmpty() {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }            
            return list.isEmpty();
        }
    }

    private class JitCollectionJiteratorIteratorTask implements Runnable {

        private final Jiterator<T> it;

        private JitCollectionJiteratorIteratorTask(Jiterator<T> it) {
            this.it = it;
        }

        @Override
        public void run() {
            int idx = 0;

            synchronized( source ) {
                try {
                    while( !complete ) {
                        if( idx < list.size() ) {
                            this.it.push(list.get(idx++));
                        }
                        else {
                            try { source.wait(100L); }
                            catch( InterruptedException e ) { /* ignore */ }
                        }
                    }
                    while( idx < list.size() ) {
                        this.it.push(list.get(idx++));
                    }
                    if( error != null ) {
                        throw new JiteratorLoadException(error);
                    }
                    this.it.complete();
                }
                catch( JiteratorLoadException e ) {
                    error = e.getCause();
                    this.it.setLoadException(e);
                }
                catch( Throwable t ) {
                    error = t.getCause();
                    this.it.setLoadException(error == null ? new JiteratorLoadException(t) : new JiteratorLoadException(error));
                }
            }
        }
    }

    @Override
    public @Nonnull Iterator<T> iterator() {
        synchronized( source ) {
            if( complete ) {
                if( error != null ) {
                    throw new JiteratorLoadException(error);
                }                
                return list.iterator();
            }
        }

        final Jiterator<T> it = new Jiterator<T>();
        DaseinUtilTasks.submit(new JitCollectionJiteratorIteratorTask(it));
        return it;
    }

    @SuppressWarnings({ "SuspiciousMethodCalls", "unused" })
    public int lastIndexOf(@Nullable Object o) {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.lastIndexOf(o);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean remove(@Nullable Object o) {
        synchronized( source ) {
            while( !complete ) {
                if( error != null ) {
                    throw new JiteratorLoadException(error);
                }
                if( list.contains(o) ) {
                    return list.remove(o);
                }
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.remove(o);
        }
    }

    @SuppressWarnings("unused")
    public @Nullable T remove(@Nonnegative int index) {
        synchronized( source ) {
            while( !complete ) {
                if( error != null ) {
                    throw new JiteratorLoadException(error);
                }
                if( list.size() > index ) {
                    return list.remove(index);
                }
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.remove(index);
        }
    }

    public boolean removeAll(@Nonnull Collection<?> c) {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.removeAll(c);
        }
    }

    public boolean retainAll(@Nonnull Collection<?> c) {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.retainAll(c);
        }
    }

    public void setSize(int size) {
        synchronized( source ) {
            this.size = size;
            source.notifyAll();
        }
    }

    public @Nonnegative int size() {
        synchronized( source ) {
            while( !complete ) {
                if( size > 0 ) {
                    return size;
                }
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.size();
        }
    }

    public @Nonnull Object[] toArray() {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.toArray();
        }        
    }

    @SuppressWarnings({"hiding", "SuspiciousToArrayCall"})
    public <T> T[] toArray(T[] a) {
        synchronized( source ) {
            while( !complete ) {
                try { source.wait(100L); }
                catch( InterruptedException e ) { /* ignore */ }
            }
            if( error != null ) {
                throw new JiteratorLoadException(error);
            }
            return list.toArray(a);
        }
    }
    
    @Override
    public @Nonnull String toString() {
        return list.toString() + " (loadingComplete=" + complete + ")";
    }
}
