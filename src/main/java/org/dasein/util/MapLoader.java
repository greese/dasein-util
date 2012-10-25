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

/* $Id: MapLoader.java,v 1.4 2006/06/20 20:12:00 greese Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * <p>
 *   Loads objects of a specific class automatically from a {@link Map}. The map should
 *   have object attribute names as keys and the values to assign to those keys as
 *   values.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/06/20 20:12:00 $
 * </p>
 * @version $Revision: 1.4 $
 * @author George Reese
 * @param <T> the type for elements being loaded by instances of the loader
 */
public class MapLoader<T> implements CacheLoader<T> {
    /**
     * The target class of loads.
     */
    private Class<T> target = null;
    
    /**
     * Constructs a loader that will load values into instances of the specified class.
     * @param cls the class for which instances will be created and loaded
     */
    public MapLoader(Class<T> cls) {
        target = cls;
    }
    
    /**
     * Creates a new instance of the class this map loader is governing and loads it
     * with values. 
     * @param args only one argument is expected, a {@link Map} with attribute names
     * as keys and object values for those attributes as values
     */
    public T load(Object ... args) {
        try {
            T item = target.newInstance();
            Map vals = (Map)args[0];
            
            load(item, vals);
            return item;
        }
        catch( InstantiationException e ) {
            throw new CacheManagementException(e);
        }
        catch( IllegalAccessException e ) {
            throw new CacheManagementException(e);
        }
    }
    
    /**
     * Loads the specified values into the specified object instance. 
     * @param item the object into which values are being loaded
     * @param vals the values to load into the object
     */
    public void load(T item, Map vals) {
        for( Object k : vals.keySet() ) {
            Class cls = item.getClass();
            String key = (String)k;
            Object val = vals.get(key);
            Field f = null;
            int mod;
            
            while( f == null ) {
                try {
                    f = cls.getDeclaredField(key);
                }
                catch( NoSuchFieldException e ) {
                    // ignore
                }
                if( f == null ) {
                    cls = cls.getSuperclass();
                    if( cls == null || cls.getName().equals(Object.class.getName()) ) {
                        break;
                    }
                }
            }
            if( f == null ) {
                continue;
            }
            mod = f.getModifiers();
            if( Modifier.isTransient(mod) || Modifier.isStatic(mod) ) {
                continue;
            }
            try {
                f.setAccessible(true);
                f.set(item, val);
            }
            catch( IllegalAccessException e ) {
                String msg = "Error setting value for " + key + ":\n";
                
                if( val == null ) {
                    msg = msg + " (null)";
                }
                else {
                    msg = msg + " (" + val + ":" +
                        val.getClass().getName() + ")";
                }
                msg = msg + ":\n" + e.getClass().getName() + ":\n";
                throw new CacheManagementException(msg + e.getMessage());
            }
            catch( IllegalArgumentException e ) {
                String msg = "Error setting value for " + key;

                if( val == null ) {
                    msg = msg + " (null)";
                }
                else {
                    msg = msg + " (" + val + ":" +
                        val.getClass().getName() + ")";
                }
                msg = msg + ":\n" + e.getClass().getName() + ":\n";
                throw new CacheManagementException(msg + e.getMessage());
            }
        }
    }
}
