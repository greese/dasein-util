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

/* $Id: DataTypeMap.java,v 1.3 2009/02/02 19:27:05 morgan Exp $ */
/* Copyright 2003-2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

import org.dasein.util.PseudoMap;

/**
 * <p>
 *   Stores data types associated with an object. In general, most objects will have a class
 *   that defines their attribute types. These types are stored in a <code>DataTypeMap</code>
 *   for the defining class and the actual values are stored in an {@link AttributeMap} in the
 *   owning object.
 * </p>
 * <p>
 *   This class was originally developed for the 
 *   <a href="http://simplicis.valtira.com" title="Simplicis web site">Simplicis Content
 *   Management System</a> in 2003 and moved into an Open Source library in 2006.
 * </p>
 * <p>
 * Last modified: $Date: 2009/02/02 19:27:05 $
 * </p>
 * @version $Revision: 1.3 $
 * @author George Reese
 */
public class DataTypeMap extends PseudoMap<String,DataType<? extends Object>> implements Serializable {
	/**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3618138931334426679L;
    
    /**
     * Actual storage of the data types.
     */
    private HashMap<String,DataType<? extends Object>> metaData = new HashMap<String,DataType<? extends Object>>();

    /**
     * Constructs a new data type map. Because instances of this class are immutable, there is
     * no way to add contents to the map once it is created.
     */
    public DataTypeMap() {
        super();
    }

    /**
     * Constructs a new data type map with type information from the provided mapping.
     * @param map the mapping from which to copy type information.
     */
    public DataTypeMap(Map<String,? extends DataType<? extends Object>> map) {
        super();
        metaData.putAll(map);
    }

    /**
     * A constructor that essentially serves as a copying mechanism to allow you to add
     * entries into a data type map. The first mapping is generally another data type map
     * that contains the original entries and the second is any new values you wish to add.
     * Values in the second mapping override any values of the same name in the first.
     * @param map1 the original mapping of data types
     * @param map2 new values to add onto or replace the originals
     */
    public DataTypeMap(Map<String,? extends DataType<? extends Object>> map1, Map<String,? extends DataType<? extends Object>> map2) {
        super();
        metaData.putAll(map1);
        metaData.putAll(map2);
    }
    
    /**
     * @param attr the attribute being checked
     * @return true if a type definition exists for the specified attribute
     */
    public boolean containsKey(Object attr) {
        return metaData.containsKey(attr);
    }

    /**
     * @param val the data type being checked
     * @return true if the specified data type is stored in this mapping
     */
    public boolean containsValue(Object val) {
        return metaData.containsValue(val);
    }

    /**
     * @return all entries in this data type map
     */
    public Set<Entry<String, DataType< ? extends Object>>> entrySet() {

        TreeSet<Map.Entry<String,DataType<? extends Object>>> toSort = new TreeSet<Map.Entry<String,DataType<? extends Object>>>(new Comparator<Entry<String,DataType<? extends Object>>>() {
            public int compare(Entry<String, DataType<? extends Object>> entry1, Entry<String, DataType<? extends Object>> entry2) {
                String grp1 = entry1.getValue().getGroup();
                if (grp1 == null) {
                    grp1 = "";
                }

                String grp2 = entry2.getValue().getGroup();
                if (grp2 == null) {
                    grp2 = "";
                }

                int c = grp1.compareTo(grp2);

                if (c != 0) {
                    return c;
                }

                Number idx1 = entry1.getValue().getIndex();
                if (idx1 == null) {
                    idx1 = 0;
                }

                Number idx2 = entry2.getValue().getIndex();
                if (idx2 == null) {
                    idx2 = 0;
                }

                c = idx1.toString().compareTo(idx2.toString());

                if (c != 0) {
                    return c;
                }

                return entry1.getKey().compareTo(entry2.getKey());
            }
        });
        toSort.addAll(metaData.entrySet());
        return toSort;

        //return new HashSet<Map.Entry<String,DataType<? extends Object>>>(metaData.entrySet());
    }
    
    /**
     * Compares this data type map to another object and checks for equality.
     * @param ob the other object to check
     * @return true if all members of both mappings are the same
     */
    public boolean equals(Object ob) {
        DataTypeMap other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (DataTypeMap)ob;
        if( metaData.size() != other.metaData.size() ) {
            return false;
        }
        for( Map.Entry<String,DataType<?>> item : metaData.entrySet() ) {
            ob = other.metaData.get(item.getKey());
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
     * @param attr the attribute being sought
     * @return the data type definition for the named attribute
     */
    public DataType<? extends Object> get(Object attr) {
        return metaData.get(attr);
    }

    /**
     * @return a unique hash code for the mapping
     */
    public int hashCode() {
        return metaData.hashCode();
    }

    /**
     * @return true if this mapping has no elements
     */
    public boolean isEmpty() {
        return metaData.isEmpty();
    }

    /**
     * @return the attributes whose data type definitions are contained in this mapping
     */
    public Set<String> keySet() {
        return new HashSet<String>(metaData.keySet());
    }
    
    /**
     * @return the number of elements in this data type map
     */
    public int size() {
        return metaData.size();
    }
    
    /**
     * @return all of the data types in this data type map
     */
    public Collection<DataType<? extends Object>> values() {
        ArrayList<DataType<? extends Object>> tmp = new ArrayList<DataType<? extends Object>>();

        tmp.addAll(metaData.values());
        return tmp;
    }

    /**
     * @return a string representation of this data type map
     */
    public String toString() {
        return metaData.toString();
    }    
}
