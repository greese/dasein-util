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

/* $Id: ConcurrentCache.java,v 1.5 2009/07/02 01:37:02 greese Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 *   Caches objects that implement the {@link CachedItem} interface and manages their
 *   life cycle. The point of a cache is to act as an automated loader of objects from
 *   some persistent repository on-demand, cache them in memory, and then release them
 *   after they are no longer needed. An application can always rely on the cache to
 *   access the most up-to-date copy of an object and provide shared references to
 *   that object.
 * </p>
 * <p>
 *   Last modified: $Date: 2009/07/02 01:37:02 $
 * </p>
 * @version $Revision: 1.5 $
 * @author George Reese
 * @param <K> the type for key values stored in the cache
 * @param <V> the type of objects stored in the cache
 */
public class ConcurrentCache<K,V> implements ConcurrentMap<K,V> {
    /**
     * The hash map that backs up this cache.
     */
    //private HashMap<K,WeakReference<V>> cache = new HashMap<K,WeakReference<V>>();
    private HashMap<K,SoftReference<V>> cache = new HashMap<K,SoftReference<V>>();

    /**
     * Clears out all elements of the cache and starts fresh.
     */
    public void clear() {
        synchronized( this ) {
            cache.clear();
        }
    }

    /**
     * This method will verify both that the key exists and the value is currently
     * value for the cache.
     * @param key the key to test for existence
     * @return true if the cache has a value with the specified key
     */
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        synchronized( this ) {
            //WeakReference ref;
            SoftReference ref;
            V item;
            
            if( !cache.containsKey(key) ) {
                return false;
            }
            ref = cache.get(key);
            item = (V)ref.get();
            if( item == null ) {
                return false;
            }
            if( item instanceof CachedItem ) {
                CachedItem ci = (CachedItem)item;
                
                if( !ci.isValidForCache() ) {
                    remove(key);
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Verifies that the specified value is in the cache.
     * @param val the desired value
     * @return true if it is in the cache and value
     */
    public boolean containsValue(Object val) {
        synchronized( this ) {
            for( V item : values() ) {
                if( item instanceof CachedItem ){
                    CachedItem ci = (CachedItem)item;

                    if( ci.isValidForCache() && ci.equals(val) ) {
                        return true;
                    }
                }
                else if( item.equals(val) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Returns a set of entries in this cache.
     * @return cache entries
     */
    public Set<Entry<K, V>> entrySet() {
        TreeSet<Entry<K,V>> set = new TreeSet<Entry<K,V>>();
        
        synchronized( this ) {
            for(K key : keySet() ) {
                if( containsKey(key) ) {
                    get(key);
                    set.add(getEntry(key));
                }
            }
            return set;
        }
    }
    
    /**
     * Retrieves the item associated with the specified key if it is currently
     * valid for the cache.
     * @param key the key whose item is being sought
     * @return the current value for that key, if any
     */
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        synchronized( this ) {
            //WeakReference ref;
            SoftReference ref;
            V item;
            
            if( !containsKey(key) ) {
                return null;
            }
            ref = cache.get(key);
            item = (V)ref.get();
            if( item == null ) {
                return null;
            }
            if( item instanceof CachedItem ) {
                CachedItem ci = (CachedItem)item;
                
                if( ci.isValidForCache() ) {
                    return item;
                }
                remove(key);
                return null;
            }
            return item;
        }
    }

    /**
     * Returns an entry value for the specified 
     * @param key the key of the desired entry
     * @return an entry for the specified key
     */
    private Entry<K, V> getEntry(K key) {
        final K k = key;
        
        return new Entry<K, V>() {
            public boolean equals(Object ob) {
                if( ob instanceof Entry ) {
                    Entry entry = (Entry)ob;
                    
                    if( !entry.getKey().equals(getKey()) ) {
                        return false;
                    }
                    if( !entry.getValue().equals(getValue()) ) {
                        return false;
                    }
                    return true;
                }
                return false;
            }

            public K getKey() {
                return k;
            }

            public V getValue() {
                return ConcurrentCache.this.get(getKey());
            }

            public V setValue(V val) {
                return ConcurrentCache.this.put(getKey(), val);
            }
        };
    }
    
    /**
     * Retrieves the value for the specified key. If no value is present, this method
     * will attempt to load a value and place it into the cache. This method appears
     * atomic in accordance with the contract of a @{link ConcurrentMap}, but any
     * required loading will actually occur outside of a synchronous block, thus allowing
     * for other operations on the cache while a load is in process. In the rare
     * instance that two loads occur simultaneously, the result of the first completed
     * load will be stored in the cache and the second will be discarded. As a result,
     * the return value of both calls will be the item loaded from the first load to
     * complete.
     * @param key the key being sought 
     * @param loader a loader to load a new value if a value is missing
     * @return the value matching the specified key or <code>null</code> if no object
     * exists in the system matching the desired key
     */
    public V getOrLoad(K key, CacheLoader<V> loader) {
        V item;
        
       // synchronized( this ) {
            if( containsKey(key) ) {
                return get(key);
            }
        //}
        item = loader.load();
        if( item != null ) {
            putIfAbsent(key, item);
            return get(key);
        }
        else {
            return null;
        }
    }

    /**
     * @return true of the cache is empty
     */
    public boolean isEmpty() {
        //synchronized( this ) {
            return cache.isEmpty();
        //}
    }

    /**
     * Places the specified value into the cache.
     * @param key the key for the item being placed into the cache
     * @param val the item to be cached
     * @return the resulting value stored in the cache
     */
    public V put(K key, V val) {
        //synchronized( this ) {
            //WeakReference<V> ref = new WeakReference<V>(val);
            SoftReference<V> ref = new SoftReference<V>(val);
            
            cache.put(key, ref);
            return get(key);
        //}
    }

    /**
     * @return all of the keys in the cache
     */
    public Set<K> keySet() {
        //synchronized( this ) {
            return cache.keySet();
        //}
    }

    /**
     * Places all elements in the specified map into this cache.
     * @param map the map to store in this cache.
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        //synchronized( this ) {
            for(K key : map.keySet() ) {
                put(key, map.get(key));
            }
        //}
    }

    /**
     * Conditionally associates the specified value with the specified key if
     * no value currently exists for the key. The actual value stored with the
     * key is returned.
     * @param key the key for which a value is to be stored
     * @param val the proposed new value
     * @return the actual value stored with key, whether the old or the new
     */
    public V putIfAbsent(K key, V val) {
        //synchronized( this ) {
            V item;
            
            if( !containsKey(key) ) {
                return put(key, val);
            }
            item = get(key);
            if( item instanceof CachedItem ) {
                CachedItem ci = (CachedItem)item;
                
                if( !ci.isValidForCache() ) {
                    put(key, val);
                    return null;
                }
            }
            return item;
        //}
    }
    
    /**
     * Removes the specified object from the cache.
     * @param key the key to be removed from the cache
     * @return the previous value or null if nothing was in there in the first place
     */
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        //synchronized( this ) {
            //WeakReference ref;
            SoftReference ref;
            V item;
            
            ref = cache.remove(key);
            if( ref == null ) {
                return null;
            }
            item = (V)ref.get();
            if( item == null ) {
                return null;
            }
            if( item instanceof CachedItem ) {
                CachedItem ci = (CachedItem)item;
                
                if( ci.isValidForCache() ) {
                    return item;
                }
            }
            return null;
        //}
    }

    /**
     * Removes the specified key only if the current value equals the specified value.
     * If the value does not match the current value, the removal will not occur.
     * @param key the key to be removed
     * @param val the value that must be matched by the current value for this key
     */
    public boolean remove(Object key, Object val) {
        String k = key.toString();
        
       // synchronized( this ) {
            V item;
            
            if( !containsKey(k) ) {
                return false;
            }
            item = get(k);
            if( val == null && item == null ) {
                remove(k);
                return true;
            }
            if( val == null || item == null ) {
                return false;
            }
            if( val.equals(item) ) {
                remove(k);
                return true;
            }
       // }
        return false;
    }

    /**
     * Replaces the specified key only if it has some current value.
     * @param key the key to replace
     * @param val the new value
     * @return whatever is stored in the cache for the key when the operation completes
     */
    public V replace(K key, V val) {
       // synchronized( this ) {
            if( !containsKey(key) ) {
                return null;
            }
            return put(key, val);
        //}
    }

    /**
     * Replaces the current value of the specified key with the proposed new value
     * only if the current value matches the specified old value.
     * @param key the key whose value should be replaced
     * @param ov the old value that should match the current value in the cache
     * @param nv the new value to put in the cache
     * @return true if the value was replaced
     */
    public boolean replace(K key, V ov, V nv) {
      //  synchronized( this ) {
            if( !remove(key, ov) ) {
                return false;
            }
            put(key, nv);
            return true;
        //}
    }

    /**
     * @return the number of elements currently in the cache
     */
    public int size() {
      //  synchronized( this ) {
            return cache.size();
       // }
    }

    /**
     * @return all of the values in the cache
     */
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<V>();
        
       // synchronized( this ) {
            for( K key: keySet() ) {
                if( containsKey(key) ) {
                    values.add(get(key));
                }
            }
            return values;
       // }
    }
    
    public String toString() {
        return (super.toString() + ": " + cache.toString());
    }
}
