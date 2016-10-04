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

/* $Id: ConcurrentMultiCache.java,v 1.11 2006/08/31 18:46:17 greese Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>
 *   A concurrent multi-cache caches objects along multiple
 *   unique keys. You provide the list of unique keys and then this object will
 *   manage the concurrent access of multiple threads into the cache.
 * </p>
 * <p>
 *   This class is backed up by multiple @{link ConcurrentCache} instances (one for
 *   each unique key) and thus behaves in accordance with the rules for that object.
 *   If, for example, you wanted to create a memory cache of employees:
 * </p>
 * <p>
 * <code>
 * public class EmployeeFactory {<br/>
 *     private ConcurrentMultiCache&lt;Employee&gt; cache = new ConcurrentMultiCache&lt;Employee&gt;(Employee.class, "employeeId", "email");<br/>
 *     <br/>
 *     public Employee getEmployeeById(Number id) {<br/>
 *       return cache.find("employeeId", id, getIdLoader(id));<br/>
 *     }<br/>
 *     <br/>
 *     public Employee getEmployeeByEmail(String email) {<br/>
 *       return cache.find("email", email, getEmailLoader(email));</br>
 *     }<br/>
 * }
 * </code>
 * </p>
 * <p>
 *   In the above example, the <code>getIdLoader()</code> and <code>getEmailLoader()</code>
 *   methods would be methods you would create to return an instance of
 *   {@link CacheLoader} that would load the desired employee from the database based
 *   on the employee ID or email address, respectively.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/08/31 18:46:17 $
 * </p>
 * @version $Revision: 1.11 $
 * @author George Reese
 * @param <T> the type for objects being stored in instances of the cache
 */
public class ConcurrentMultiCache<T> {
    /**
     * A mapping of the unique identifer names to concurrent caches.
     */
    private HashMap<String,ConcurrentMap<Object,T>> caches = new HashMap<String,ConcurrentMap<Object,T>>(0);
    
    /**
     * The ordered list of unique identifiers supported by this cache.
     */
    private ArrayList<String>                         order  = new ArrayList<String>(0);
    /**
     * The class being managed by this cache, if any.
     */
    private Class<T>                                  target = null;

    /**
     * Constructs a concurrent multi-cache that caches for unique keys specified by the
     * attributes. This constructor allows for automated loading into the cache.
     * @param cls the class object for objects stored in this cache
     * @param attrs the unique keys that this cache will cache objects on
     */
    public ConcurrentMultiCache(Class<T> cls, Collection<String> attrs) {
        super();
        target = cls;
        for( String attr : attrs ) {
            if( !caches.containsKey(attr) ) {
                ConcurrentCache<Object,T> cache = new ConcurrentCache<Object,T>();

                caches.put(attr, cache);
                order.add(attr);
            }
        }
    }

    /**
     * Constructs a concurrent multi-cache that caches for unique keys specified by the
     * attributes.
     * @param attrs the unique keys that this cache will cache objects on
     */
    public ConcurrentMultiCache(String ... attrs) {
        super();
        for( String attr : attrs ) {
            if( !caches.containsKey(attr) ) {
            	ConcurrentCache<Object,T> cache = new ConcurrentCache<Object,T>();

                caches.put(attr, cache);
                order.add(attr);
            }
        }
    }

    /**
     * Constructs a concurrent multi-cache that caches for unique keys specified by the
     * attributes. This constructor allows for automated loading into the cache.
     * @param cls the class object for objects stored in this cache
     * @param attrs the unique keys that this cache will cache objects on
     */
    public ConcurrentMultiCache(Class<T> cls, String ... attrs) {
        super();
        target = cls;
        for( String attr : attrs ) {
            if( !caches.containsKey(attr) ) {
            	ConcurrentCache<Object,T> cache = new ConcurrentCache<Object,T>();

                caches.put(attr, cache);
                order.add(attr);
            }
        }
    }

    /**
     * Places the specified item in the cache. It will not replace an existing
     * item in the cache. Instead, if an item already exists in the cache, it
     * will make sure that item is cached across all identifiers and then return
     * the cached item.
     * @param item the item to be cached
     * @return whatever item is in the cache after this operation
     */
    public T cache(T item) {
        HashMap<String,Object> keys;

        if( item == null ) {
            throw new NullPointerException("Multi caches may not have null values.");
        }
        keys = getKeys(item);
        synchronized( this ) {
            item = getCurrent(item);
            for( String key : caches.keySet() ) {
            	ConcurrentMap<Object,T> cache = caches.get(key);

                cache.put(keys.get(key), item);
            }
            return item;
        }
    }

    /**
     * Finds the object from the cache with the specified unique identifier value
     * for the default unique identifier attribute. This method will throw an
     * exception if there is more than one unique identifier associated with this
     * cache.
     * @param val the value matching the desired object from the cache
     * @return the matching object from the cache
     * @throws CacheManagementException this multi-cache supports multiple unique IDs
     */
    public T find(Object val) {
        return find(val, null);
    }

    /**
     * Finds the object in the cache matching the values in the specified value map.
     * If a matching argument is not in the cache, this method will instantiate an
     * instance of the object and assign the mapping values to that instantiated
     * object. In order for this to work, the keys in this mapping must have the same
     * names as the attributes for the instantiated object
     * @param vals the mapping that includes the attribute/value pairs
     * @return an object matching those values in the cache
     */
    public T find(Map<String,Object> vals) {
        String key = order.get(0);
        Object val = vals.get(key);
        CacheLoader<T> loader;

        if( val == null ) {
            throw new CacheManagementException("No value specified  for key: " + key);
        }
        loader = new MapLoader<T>(target);
        return find(key, val, loader, vals);
    }

    /**
     * Returns the object identified by the specified key/value pair if it is currently
     * in memory in the cache. Just because this value returns <code>null</code> does
     * not mean the object does not exist. Instead, it may be that it is simply not
     * cached in memory.
     * @param key they unique identifier attribute on which you are searching
     * @param val the value of the unique identifier on which you are searching
     * @return the matching object from the cache
     */
    public T find(String key, Object val) {
        return find(key, val, null);
    }

    /**
     * Calls {@link #find(String,Object,CacheLoader)} using the only unique
     * identifier attribute as passed to this cache's constructor. If more than one
     * unique identifier attribute was passed, this method will throw an exception
     * @param val the value of the unique key being sought
     * @param loader the loader to load new instances from the persistent store
     * @return a matching object, if any
     * @throws CacheManagementException this cache supports multiple unique identifiers
     */
    public T find(Object val, CacheLoader<T> loader) {
        if( order.size() != 1 ) {
            throw new CacheManagementException("You may only call this method when the cache is managing one unique identifier.");
        }
        return find(order.get(0), val, loader);
    }

    /**
     * Seeks the item from the cache that is identified by the specified key having
     * the specified value. If no match is found, the specified loader will be called to
     * place an item in the cache. You may pass in  <code>null</code> for the loader.
     * If you do, only an object in active memory will be returned.
     * @param key the name of the unique identifier attribute whose value you have
     * @param val the value of the unique identifier that identifiers the desired item
     * @param loader a loader to load the desired object from the persistence store if it
     * is not in memory
     * @return the object that matches the specified key/value
     */
    public T find(String key, Object val, CacheLoader<T> loader) {
        return find(key, val, loader, null, null);
    }

    /**
     * Seeks the item from the cache that is identified by the specified key having
     * the specified value. If no match is found, the specified loader will be called
     * with the specified arguments in order to place an instantiated item into the cache.
     * @param key the name of the unique identifier attribute whose value you have
     * @param val the value of the unique identifier that identifiers the desired item
     * @param loader a loader to load the desired object from the persistence store if it
     * is not in memory
     * @param args any arguments to pass to the loader
     * @return the object that matches the specified key/value
     */
    public T find(String key, Object val, CacheLoader<T> loader, Object ... args) {
    	//System.out.println("cm args:" + args[0] + " " + args[1]);
    	ConcurrentMap<Object,T> cache = caches.get(key);
        T item;

        if( val instanceof BigDecimal ) {
            val = ((BigDecimal)val).longValue();
        }
        item = cache.get(val);
        if( item == null  && loader != null ) {
            item = loader.load(args);
            if( item == null ) {
                return null;
            }
            synchronized( this ) {
                item = getCurrent(item);
                put(item);
            }
        }
        return item;
    }

    /**
     * Provides the current version of the specified item stored in the cache.
     * If no item matches the passed in item, then the passed in item is returned
     * but not cached. This better be called from a synchronized block!
     * @param item the item being sought
     * @return the currently cached object that is equivalent to the passed in object
     */
    private T getCurrent(T item) {
        for( String key: order ) {
        	ConcurrentMap<Object,T> cache = caches.get(key);
            Object val = getValue(key, item);
            T tmp;

            if( val instanceof BigDecimal ) {
                val = ((BigDecimal)val).longValue();
            }
            tmp = cache.get(val);
            if( tmp != null ) {
               return tmp;               
            }
        }
        return item;
    }

    /**
     * Provides the values for all of the unique identifiers managed by the cache.
     * @param item the item whose key values are being sought
     * @return a mapping of key names to item values
     */
    public HashMap<String,Object> getKeys(T item) {
        HashMap<String,Object> keys = new HashMap<String,Object>(caches.size());

        for( String key: caches.keySet() ) {
            keys.put(key, getValue(key, item));
        }
        return keys;
    }

    public Class<T> getTarget() {
        return target;
    }

    /**
     * Provides the actual value for the specified unique ID key for the specified object.
     * @param key the name of the unique identifier
     * @param item the object who's unique identifier value is sought
     * @return the unique identifier for the object's attribute matching the key
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getValue(String key, T item) {
        Class cls = item.getClass();

        while( true ) {
            try {
                Field f = cls.getDeclaredField(key);
                int m = f.getModifiers();

                if( Modifier.isTransient(m) || Modifier.isStatic(m) ) {
                    return null;
                }
                f.setAccessible(true);
                return f.get(item);
            }
            catch( Exception e ) {
                Class[] params = new Class[0];
                String mname;

                mname = "get" + key.substring(0,1).toUpperCase() + key.substring(1);
                try {
                    Method method = cls.getDeclaredMethod(mname, params);
                    Object[] args = new Object[0];

                    return method.invoke(item, args);
                }
                catch( IllegalAccessException e2 ) {
                    // ignore
                }
                catch( SecurityException e2 ) {
                    // ignore
                }
                catch( NoSuchMethodException e2 ) {
                    // ignore
                }
                catch( IllegalArgumentException e2 ) {
                    // ignore
                }
                catch( InvocationTargetException e2 ) {
                    // ignore
                }
                cls = cls.getSuperclass();
                if( cls == null || cls.getName().getClass().equals(Object.class.getName()) ) {
                    throw new CacheManagementException("No such property: " + key);
                }
            }
        }
    }

    /**
     * Places the specified item into the cache regardless of current cache state.
     * @param item the item being placed into the cache
     */
    private void put(T item) {
        HashMap<String,Object> keys;

        if( item == null ) {
            throw new NullPointerException("Multi caches may not have null values.");
        }
        keys = getKeys(item);
        synchronized( this ) {
            for( String key : caches.keySet() ) {
            	ConcurrentMap<Object,T> cache = caches.get(key);
                Object ob = keys.get(key);

                if( ob instanceof BigDecimal ) {
                    ob = ((BigDecimal)ob).longValue();
                }
                cache.put(ob, item);
            }
        }
    }

    /**
     * Releases the specified item from the cache. If it is still in the persistent
     * store, it will be retrieved back into the cache on next query. Otherwise,
     * subsequent attempts to search for it in the cache will result in <code>null</code>.
     * @param item the item to be released from the cache.
     */
    public void release(T item) {
        HashMap<String,Object> keys = getKeys(item);

        synchronized( this ) {
            for( String key : order ) {
            	ConcurrentMap<Object,T> cache = caches.get(key);

                cache.remove(keys.get(key));
            }
        }
    }

    /**
     * Releases all cached keys and objects. Please use this sparingly as you will cause all of your data to
     * reload.  In certain cases, this is a very useful method that will avoid key leakage.  Take, for example, users
     * hitting a website and you want to track each click.  If you keep all the clicks around, you will eventually
     * run out of RAM as Java will clear out the SoftReferences to the clicks, but the caches will keep the keys
     * in the internal Maps.  Not good.
     */
    public void releaseAll() {
    	synchronized( this ) {
    		for (ConcurrentMap<Object,T> cache : caches.values()) {
    			cache.clear();
    		}
    	}
    }

    public String toString() {
        return caches.toString();
    }
}