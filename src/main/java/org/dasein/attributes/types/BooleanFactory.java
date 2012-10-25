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

/* $Id: BooleanFactory.java,v 1.4 2009/01/30 23:01:49 morgan Exp $ */
/* Copyright 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeFactory;
import org.dasein.attributes.InvalidAttributeException;
import org.dasein.util.Translator;

/**
 * <p>
 *   Represents a true/false value.
 * </p>
 * <p>
 *   Last modified: $Date: 2009/01/30 23:01:49 $
 * </p>
 * @version $Revision: 1.4 $
 * @author George Reese
 */
public class BooleanFactory extends DataTypeFactory<Boolean> {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4049636802178726201L;
    
    /**
     * The name of this type is 'boolean'.
     */
    static public final String TYPE_NAME = "boolean";
    
    /**
     * Constructs a new factory instance for boolean attributes.
     */
    public BooleanFactory() {
        super();
    }
    
    /**
     * @return a display name for this data type
     */
    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    /**
     * @return the type name, 'boolean'
     */
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    /**
     * Technically, you can have a multi-lingual or multi-valued boolean, but why would you?
     * @param ml true if the boolean is multi-lingual
     * @param mv true if the boolean can support multiple values
     * @param req true if the boolean is required
     * @param params unused
     * @return a boolean instance
     */
    public DataType<Boolean> getType(boolean ml, boolean mv, boolean req, String... params) {
        return getType(null, null, ml, mv, req, params);
    }

    /**
     * Technically, you can have a multi-lingual or multi-valued boolean, but why would you?
     * @param ml true if the boolean is multi-lingual
     * @param mv true if the boolean can support multiple values
     * @param req true if the boolean is required
     * @param params unused
     * @return a boolean instance
     */
    public DataType<Boolean> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        return new BooleanAttribute(grp, idx, ml, mv, req);
    }
    
    /**
     * <p>
     *   Implements the rules around a boolean value.
     * </p>
     * <p>
     *   Last modified: $Date: 2009/01/30 23:01:49 $
     * </p>
     * @version $Revision: 1.4 $
     * @author George Reese
     */
    static public class BooleanAttribute extends DataType<Boolean> {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3257848766349193780L;
        
        /**
         * A collection with the values <code>true</code> and <code>false</code>.
         */
        static private final Collection<Boolean> choices = new ArrayList<Boolean>();
        
        static {
            choices.add(true);
            choices.add(false);
        }
        
        /**
         * Constructs a boolean attribute instance.
         * @param grp the group of the data type.
         * @param idx the index of the data type.
         * @param ml true if the boolean is multi-lingual
         * @param mv true if the boolean supports multiple values
         * @param req true if a value is required
         */
        public BooleanAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req) {
            super(TYPE_NAME, grp, idx, ml, mv, req, (String[])null);
        }
        
        /**
         * @return <code>true</code> and <code>false</code>
         */
        public Collection<Boolean> getChoices() {
            return choices;
        }
        
        /**
         * @return the factory that constructed this boolean attribute
         */
        @SuppressWarnings("unchecked")
        public DataTypeFactory<Boolean> getFactory() {
            return (DataTypeFactory<Boolean>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        /**
         * @return a {@link org.dasein.attributes.DataType.InputType#SELECT} instance
         */
        public InputType getInputType() {
            return (InputType)InputType.SELECT;
        }
        
        /**
         * Given a raw value from some source, this method will provide a Java
         * {@link java.lang.Boolean} instance. This raw value can be a string
         * ('true' or 'false'), a boolean, or a number (non-zero is true, zero is false).
         * @param val the raw value
         * @return the corresponding boolean value
         */
        public Boolean getValue(Object val) {
            if( val == null ) {
                return null;
            }
            else if( val instanceof Boolean ) {
                return (Boolean)val;
            }
            else if( val instanceof String ) {
                String str = (String)val;
                
                return new Boolean(str.equalsIgnoreCase("true"));
            }
            else if( val instanceof Number) {
                Number num = (Number)val;
                
                return new Boolean(num.intValue() != 0);
            }
            else {
                throw new InvalidAttributeException("Not boolean: " + val);
            }
        }
        
        /**
         * Verifies that the specified value is valid according to the rules of this 
         * data type. In practice, the only check it performs is whether or not a
         * value is required when the parameter is null.
         * @param b the value being validated
         * @return true if the specified value is valid for this type's rules
         */
        public boolean isValidChoice(Boolean b) {
            return b != null || !isRequired();
        }
        
        public String toString() {
            return "Boolean (true/false)";
        }
    }
}
