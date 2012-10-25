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

/* $Id: DataType.java,v 1.7 2009/02/02 19:27:05 morgan Exp $ */
/* Copyright 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;

import org.dasein.util.Translator;

/**
 * <p>
 *   Represents a data type for the Dasein attributes system. A data type governs what
 *   kind of values are valid for any attribute having this data type. To create custom
 *   data types, you must extend this class as well as the {@link DataTypeFactory} class.
 * </p>
 * <p>
 *   Last modified: $Date: 2009/02/02 19:27:05 $
 * </p>
 * @version $Revision: 1.7 $
 * @author George Reese
 * @param <V> the Java data type that this data type operates on
 */
public abstract class DataType<V> implements Serializable {
    /**
     * <p>
     *   Represents a type of input field associated with inputting values associated
     *   with this attributes system.
     * </p>
     * <p>
     *   Last modified: $Date: 2009/02/02 19:27:05 $
     * </p>
     * @version $Revision: 1.7 $
     * @author George Reese
     */
    static public enum InputType {
        DATE,
        TEXT,
        TEXTAREA,
        SELECT,
        MULTI_SELECT,
        CHECKBOX,
        PASSWORD,
        PAIR,
        TIME
    }

    /**
     * The group this attribute belongs to (optional).
     */
    private String    group        = null;

    /**
     * The order of this attribute in its group (optional).
     */
    private Number    index        = null;

    /**
     * Does this data type support multi-lingual content?
     */
    private boolean   multiLingual = false;
    /**
     * Does this data type allow multiple values?
     */
    private boolean   multiValued  = false;
    /**
     * The name identifying the data type.
     */
    private String    name         = null;
    /**
     * The parameters used to constrain the data type.
     */
    private String[]  parameters   = null;
    /**
     * Is a value required for this data type or can it be null?
     */
    private boolean   required     = false;
    
    /**
     * Constructs a new data type instance. Implementations will generally override this
     * with custom constructors that do something meaningful with the type parameters.
     * @param nom the name of the data type
     * @param grp the group of the data type
     * @param idx the index of the data type
     * @param ml true if the type is multi-lingual
     * @param mv true if the type allows more than one value
     * @param req true if the type requires a value (cannot be null)
     * @param params a list of parameters constraining this type
     */
    public DataType(String nom, String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        super();
        name = nom;
        group = grp;
        index = idx;
        multiLingual = ml;
        multiValued = mv;
        required = req;
        parameters = params;
    }

    /**
     * Two data type instances are considered identical if everything about them is the same.
     * @return true if both data type instances represent the same exact data type
     */
    public boolean equals(Object ob) {
        DataType other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (DataType)ob;
        if( !name.equals(other.name) ) {
            return false;
        }
        if (group != null && other.group != null && !group.equals(other.group)){
            return false;
        }
        if (index != null && other.index != null && !index.equals(other.index)){
            return false;
        }
        if( multiLingual != other.multiLingual ) {
            return false;
        }
        if( multiValued != other.multiValued ) {
            return false;
        }
        if( required != other.required ) {
            return false;
        }
        if( parameters == null ) {
            return (other.parameters == null);
        }
        if( other.parameters == null ) {
            return false;
        }
        if( parameters.length != other.parameters.length ) {
            return false;
        }
        for( int i=0; i<parameters.length; i++ ) {
            if( parameters[i] == null || parameters[i].equals("null") ) {
                if( other.parameters[i] == null ) {
                    continue;
                }
                if( other.parameters[i].equals("null") ) {
                    continue;
                }
                return false;
            }
            else if( other.parameters[i] == null ) {
                return false;
            }
            if( !parameters[i].equals(other.parameters[i]) ) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Provides a list of values considered valid for this data type. If any object of the type
     * governed by this data type is allowed, this method will return <code>null</code>.
     * @return a list of allowed values or <code>null</code> to allow any value
     */
    public abstract Collection<V> getChoices();
    
    /**
     * @return the factory object that owns this data type
     */
    public abstract DataTypeFactory<V> getFactory();
    
    /**
     * @return the type of input that a user interface should provide to prompt a user for
     * a value
     */
    public abstract InputType getInputType();

    /**
     * @return the name of this data type
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the group of this data type
     */
    public final String getGroup() {
        return group;
    }

    /**
     * @return the index of this data type
     */
    public final int getIndex() {
        return (index != null ? index.intValue() : -1);
    }

    /**
     * Type parameters tell a data type how to constrain values. What these parameters are
     * and how they are interpreted are left entirely up to the implementing classes.
     * @return the constraining parameters for this data type
     */
    public final Collection<String> getParameters() {
        ArrayList<String> params = new ArrayList<String>();
        
        if( parameters == null ) {
            return params;
        }
        // we copy to guarantee that the caller does not screw up our internal storage
        params.addAll(Arrays.asList(parameters));
        return params;
    }
    
    /**
     * @return the recommended size for an input field
     */
    public int getSize() {
        return 20;
    }
    
    /**
     * Provides a {@link Translator} instance for the requested value. This method is
     * generally called to turn an arbitrary object into a multi-lingual instance of
     * of the variable. Basically, when the system is trying to convert arbitrary
     * data&mdash;such as user input or values from a persistence engine&mdash;into
     * proper values for this type, it will call either this method or {@link #getValue(Object)}.
     * Which method is called is dependent on whether this data type is multi-lingual.
     * If the data type is multi-lingual, this method is called. This method will, in turn,
     * call {@link #getValue(Object)} and wrap it in a {@link Translator}.
     * @param src the raw data to be converted
     * @param loc the locale associated with this value
     * @param curr any current values for other languages
     * @return a combined multi-lingual value
     */
    @SuppressWarnings("unchecked")
    public Translator<V> getTranslatedValue(Object src, Locale loc, Translator<?> curr) {
        if( curr != null ) {
            Map<Locale,Object> map = (Map<Locale, Object>)curr.toMap();
            Object ob = getValue(src);
            
            map.put(loc, ob);
            return new Translator(map);
        }
        else {
            return new Translator<V>(loc, getValue(src));
        }
    }
    
    /**
     * Implementors will implement this method to convert a raw value into a valid value of
     * this data type. Implementations should throw an {@link InvalidAttributeException} if
     * they are unable to interpret the raw value.
     * @param src the raw value, such as user input or a string from a persistent store
     * @return a converted value of this data type
     */
    public abstract V getValue(Object src);
    
    /**
     * When a data type is multi-valued, this method is called to convert raw data into a 
     * collection of values matching this data type. If the raw value is a string, this
     * method assumes that the string represents a comma-delimited, multi-value
     * attribute.
     * @param src the raw source value
     * @return the converted value
     */
    @SuppressWarnings("unchecked")
    public Collection<V> getValues(Object src) {
        ArrayList<V> values = new ArrayList<V>();
        
        if( src instanceof String ) {
            String[] words = ((String)src).split(",");
            
            if( words.length < 1 ) {
                values.add(getValue(src));
            }
            else {
                for( String word : words ) {
                    values.add(getValue(word));
                }
            }
            return values;
        }
        else if( src instanceof Collection ) {
            for( Object ob : (Collection<Object>)src ) {
                values.add(getValue(ob));
            }
            return values;
        }
        throw new InvalidAttributeException("Invalid attribute: " + src);
    }
    
    /**
     * @return a unique hash code for this instance of this class
     */
    public int hashCode() {
        return getName().hashCode();
    }
    
    /**
     * @return true if this data type supports multiple values for different languages
     */
    public final boolean isMultiLingual() {
        return multiLingual;
    }
    
    /**
     * @return true if this data type allows for multiple values
     */
    public final boolean isMultiValued() {
        return multiValued;
    }
    
    /**
     * @return true if this data type must have a value (cannot be null)
     */
    public final boolean isRequired() {
        return required;
    }
    
    /**
     * <p>
     * Checks the specified value to see if it is valid for this data type. In general, 
     * implementing classes will check the following:
     * </p>
     * <ul>
     * <li>that the value is not required in the instance of a <code>null</code> value</li>
     * <li>that the value is a member of the choices provided by {@link #getChoices()}</li>
     * <li>any other criteria an implementor might want to validate</li>
     * </ul>
     * @param arg the value being validated
     * @return true if the value is valid for this data type
     */
    public abstract boolean isValidChoice(V arg);
    
    /**
     * @return a string representation of this data type
     */
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        
        tmp.append(name);
        tmp.append("=");
        if (group != null) {
            tmp.append(group);
            tmp.append("/");
        }
        if (index != null) {
            tmp.append(String.valueOf(index));
            tmp.append("/");
        }
        tmp.append(String.valueOf(multiLingual));
        tmp.append("/");
        tmp.append(String.valueOf(multiValued));
        tmp.append("/");
        tmp.append(String.valueOf(required));
        tmp.append("/");
        tmp.append("{");
        if( parameters != null ) {
            for( String param : parameters ) {
                tmp.append(param);
                tmp.append(",");
            }
        }
        tmp.append("}");
        return tmp.toString();
    }
}
