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

// Java imports
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>
 * Provides a cache of objects that will expire its contents as those
 * contents fail to be used. This cache uses
 * {@link java.lang.ref.SoftReference} to guarantee
 * that cached items will be removed when they have not been
 * referenced for a long time. 
 * </p>
 * <p>
 * This class is not synchronized and therefore should be synchronized
 * by the application for multi-threaded use.
 * </p>
 * <p>
 * Last modified $Date: 2005/08/15 16:19:34 $
 * </p>
 * @version $Revision: 1.5 $
 * @author George Reese
 * @param <T> the type of object being stored in the cache
 */
public class Cache<T> implements Collection<T>, Serializable {
    /**
	 * Serialization identifier for the class.
	 */
	static private final long serialVersionUID = 3256437010649592628L;
	/**
     * A hash map indexing references by unique keys.
     */
    private HashMap<Object,SoftReference<T>> cache = new HashMap<Object,SoftReference<T>>(0);

    /*
     * Constructs a new empty cache.
     */
    public Cache() {
        super();
    }

    /**
     * Unsupported.
     * @param ob ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean add(T ob) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Unsupported.
     * @param coll ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always\
     */
    public boolean addAll(Collection<? extends T> coll) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Caches the specified object identified by the specified key.
     * @param key a unique key for this object
     * @param val the object to be cached
     */
    public void cache(Object key, T val) {
        cache.put(key, new SoftReference<T>(val));
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        cache.clear();
    }
    
    /**
     * Checks the specified object against the cache and verifies that it
     * is in the cache. This method will return
     * <span class="keyword">false</span> if the object was once in the cache
     * but has expired due to inactivity.
     * @param ob the object to check for in the cache
     * @return true if the object is in the cache
     */
    public boolean contains(Object ob) {
        for( SoftReference<T> ref : cache.values() ) {
            T item = ref.get();

            if( item != null && ob.equals(item) ) {
                if( item instanceof CachedItem ) {
                    if( !((CachedItem)item).isValidForCache() ) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks the passed in collection and determines if all elements
     * of that collection are contained within this cache. Care should
     * be taken in reading too much into a failure. If one of the elements
     * was once in this cache but has expired due to inactivity, this
     * method will return false.
     * @param coll the collection to test
     * @return true if all elements of the tested collection are in the cache
     */
    public boolean containsAll(Collection<?> coll) {
        for( Object item : coll ) {
            if( !contains(item) ) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if an object with the specified key is in the cache.
     * @param key the object's identifier
     * @return true if the object is in the cache
     */
    public boolean containsKey(Object key) {
        if( !cache.containsKey(key) ) {
            return false;
        }
        else {
            SoftReference<T> ref = cache.get(key);
            T ob = ref.get();
            
            if( ob == null ) {
                release(key);
                return false;
            }
            if( ob instanceof CachedItem ) {
                if( !((CachedItem)ob).isValidForCache() ) {
                    release(key);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Provides the cached object identified by the specified key. This
     * method will return <span class="code">null</span> if the
     * specified object is not in the cache.
     * @param key the unique identifier of the desired object
     * @return the cached object or null
     */
    public T get(Object key) {
        SoftReference<T> ref = cache.get(key);
        T ob;
        
        if( ref == null ) {
            return null;
        }
        ob = ref.get();
        if( ob == null ) {
            release(key);
        }
        if( ob instanceof CachedItem ) {
            if( !((CachedItem)ob).isValidForCache() ) {
                release(key);
                return null;
            }
        }
        return ob;
    }

    /**
     * @return true if the cache is empty
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * Provides all of the valid objects in the cache.
     * This method will not be the snappiest method in the world.
     * @return all valid objects in the cache
     */
    public Iterator<T> iterator() {
        return toList().iterator();
    }
    
    /**
     * Releases the specified object from the cache.
     * @param key the unique identified for the item to release
     */
    public void release(Object key) {
        cache.remove(key);
    }

    /**
     * Unsupported.
     * @param ob ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean remove(Object ob) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     * @param coll ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean removeAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported.
     * @param coll ignored
     * @return never returns
     * @throws java.lang.UnsupportedOperationException always
     */
    public boolean retainAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the number of elements in the cache
     */
    public int size() {
        return toList().size();
    }

    /**
     * @return the cache as an array
     */
    public Object[] toArray() {
        return toList().toArray();
    }

    /**
     * @return the cache as an array
     */
    public <E> E[] toArray(E[] arr) {
        return toList().toArray(arr);
    }

    /**
     * @return the cache as an array
     */
    private ArrayList<T> toList() {
        ArrayList<T> tmp = new ArrayList<T>();

        for( SoftReference<T> ref : cache.values() ) {
            T ob = ref.get();
            
            if( ob != null ) {
                if( ob instanceof CachedItem ) {
                    if( !((CachedItem)ob).isValidForCache() ) {
                        continue;
                    }
                }
                tmp.add(ob);
            }
        }
        return tmp;
    }

    /**
     * Displays the current contents of the cache.
     * @return a string representation of the current cache contents
     */
    public String toString() {
        if( cache == null ) {
            return "[NULL CACHE]";
        }
        return cache.toString();
    }
}
