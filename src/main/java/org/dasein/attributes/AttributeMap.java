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

/* $Id: AttributeMap.java,v 1.3 2006/05/03 05:12:49 greese Exp $ */
/* Copyright 2003-2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dasein.util.PseudoMap;

/**
 * <p>
 *   An immutable mapping of attribute names to values. The main value of this
 *   class over a straight {@link Map} is that it is immutable and that it constrains
 *   keys as strings. It is therefore very useful as storage for object attributes so that
 *   you may return the full mapping from method calls without doing any copying.
 * </p>
 * <p>
 *   This class was originally developed for the 
 *   <a href="http://simplicis.valtira.com" title="Simplicis web site">Simplicis Content
 *   Management System</a> in 2003 and moved into an Open Source library in 2006.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/05/03 05:12:49 $
 * </p>
 * @version $Revision: 1.3 $
 * @author George Reese 
 */
public class AttributeMap extends PseudoMap<String,Object> implements Serializable {
	/**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3618138931334426679L;

    static public class AttributeWrapper<T> implements Serializable {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 7322346154985450360L;
        
        private DataType<T> type  = null;
        private Object      value = null;
        
        public AttributeWrapper(DataType<T> t, Object val) {
            super();
            type = t;
            value = val;
        }
        
        public boolean equals(Object ob) {
            if( ob == null ) {
                return false;
            }
            if( ob == this ) {
                return true;
            }
            if( ob instanceof AttributeWrapper ) {
                return value.equals(((AttributeWrapper)ob).value);
            }
            else {
                return getAttribute().equals(ob);
            }
        }
        
        public T getAttribute() {
            return type.getValue(value);
        }
        
        public DataType<T> getType() {
            return type;
        }
        
        public Object getValue() {
            return value;
        }
        
        public String toString() {
            return getAttribute().toString();
        }
    }
    
    static public class AmapEntry implements Map.Entry<String,Object> {
        private String       key   = null;
        private Object       value = null;
        
        public AmapEntry(String key, Object val) {
            super();
            this.key = key;
            value = val;
        }
        
        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object val) {
            throw new UnsupportedOperationException("Attribute maps are immutable.");
        }
    }
    
    /**
     * The underlying mapping that manages this attribute map.
     */
    private HashMap<String,Object> metaData = new HashMap<String,Object>();

    /**
     * Constructs an empty attribute map. Note that because this class is immutable, the
     * resulting mapping is forever empty.
     */
    public AttributeMap() {
        super();
    }
    
    /**
     * Constructs an attribute map that is copied from the specified mapping.
     * @param map the mapping to copy
     */
    public AttributeMap(Map<String,? extends Object> map) {
        super();
        metaData.putAll(map);
    }

    /**
     * Constructs an attribute map that is copied from two different mappings. This constructor
     * is useful when trying to add to an existing mapping. You will generally pass an existing
     * mapping as the first argument and any new values as the second argument.
     * @param map1 the existing mapping values
     * @param map2 the new values you wish to add to the original
     */
    public AttributeMap(Map<String,? extends Object> map1, Map<String,? extends Object> map2) {
        super();
        metaData.putAll(map1);
        metaData.putAll(map2);
    }
    
    /**
     * @return the name of all attributes in this map
     */
    public Iterator<String> attributes() {
        return keySet().iterator();
    }
    
    /**
     * @param attr the name of the attribute being validated
     * @return true if the mapping contains the specified attribute 
     */
    public boolean containsKey(Object attr) {
        return metaData.containsKey(attr);
    }

    /**
     * @param val the name of the value being checked
     * @return true if the specified value is in this mapping
     */
    public boolean containsValue(Object val) {
        for( Object ob : metaData.values() ) {
            if( val instanceof Collection ) {
                if( !(ob instanceof Collection) ) {
                    continue;
                }
                if( ob.equals(val) ) {
                    return true;
                }
            }
            else {
                if( ob instanceof AttributeWrapper ) {
                    ob = ((AttributeWrapper)ob).getAttribute();
                }
                if( ob.equals(val) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return a set containing all mapping entries
     */
    public Set<Map.Entry<String,Object>> entrySet() {
        HashSet<Map.Entry<String,Object>> tmp =  new HashSet<Map.Entry<String,Object>>();
        
        for( Map.Entry<String,Object> entry : metaData.entrySet() ) {
            tmp.add(new AmapEntry(entry.getKey(), get(entry.getKey())));
        }
        return tmp;
    }
    
    /**
     * Compares the selected object. This attribute map is considered equal to another if and
     * only if they have the same set of keys with the same exact values.
     */
    public boolean equals(Object ob) {
        AttributeMap other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (AttributeMap)ob;
        if( metaData.size() != other.metaData.size() ) {
            return false;
        }
        for( Map.Entry<String,Object> item : entrySet() ) {
            ob = other.get(item.getKey());
            if( ob == null ) {
                if( item.getValue() != null ) {
                    return false;
                }
            }
            else if( item.getValue() == null ) {
                return false;
            }
            else if( !ob.equals(item.getValue()) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param attr the name of the attribute to retrieve
     * @return any value associated the specified attribute name
     */
    public Object get(Object attr) {
        Object ob = metaData.get(attr);
        
        if( ob instanceof Collection ) {
            ArrayList<Object> tmp = new ArrayList<Object>();
            
            for( Object item : (Collection)ob ) {
                if( item instanceof AttributeWrapper ) {
                    item = ((AttributeWrapper)item).getAttribute();
                }
                tmp.add(item);
            }
            return tmp;
        }
        else if( ob instanceof AttributeWrapper ) {
            ob = ((AttributeWrapper)ob).getAttribute();
        }
        return ob;
    }

    /**
     * This method just calls {@link #get(Object)}.
     * @deprecated this is an old method, not sure why it had to be added
     * @param attr the name of the desired attribute
     * @return the value associated with the specified attribute name
     */
    public Object getAttribute(String attr) {
        return get(attr);
    }
    
    /**
     * @return the hash code
     */
    public int hashCode() {
        return metaData.hashCode();
    }

    /**
     * @return true if this mapping is empty
     */
    public boolean isEmpty() {
        return metaData.isEmpty();
    }

    /**
     * @return the list of mapping attribute names
     */
    public Set<String> keySet() {
        return new HashSet<String>(metaData.keySet());
    }
    
    /**
     * @return the number of elements in this mapping
     */
    public int size() {
        return metaData.size();
    }
    
    /**
     * @return all values in this mapping
     */
    public Collection<Object> values() {
        ArrayList<Object> tmp = new ArrayList<Object>();

        for( String attr : keySet() ) {
            tmp.add(get(attr));
        }
        return tmp;
    }

    /**
     * Converts this mapping into a human-readable string.
     * @return a human-readable string of key-value pairs
     */
    public String toString() {
        StringBuffer str = new StringBuffer();
        
        str.append("{");
        for( Map.Entry<String,Object> entry : entrySet() ) {
            Object val = entry.getValue();
            
            str.append(entry.getKey());
            str.append("=");
            str.append(val == null ? "null" : val.toString());
            str.append(",");
        }
        str.append("}");
        return str.toString();
    }    
}
