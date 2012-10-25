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

/* $Id: CurrencyFactory.java,v 1.6 2009/01/30 23:01:49 morgan Exp $ */
/* Copyright 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Locale;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeFactory;
import org.dasein.util.Translator;

/**
 * <p>
 *   Represents currency values as {@link java.util.Currency} instances.
 * </p>
 * <p>
 *   Last modified: $Date: 2009/01/30 23:01:49 $
 * </p>
 * @version $Revision: 1.6 $
 * @author George Reese
 */
public class CurrencyFactory extends DataTypeFactory<Currency> {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3905801976582059057L;
    
    /**
     * The type name: 'currency'
     */
    static public final String TYPE_NAME = "currency";
    
    /**
     * Constructs a currency factory instance.
     */
    public CurrencyFactory() {
        super();
    }

    /**
     * @return a display name for the data type
     */
    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    /**
     * @return the type name: 'currency'
     */
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    /**
     * Provides a currency data type instance that supports the specified type rules.
     * This data type allows for zero or one type parameters. If no type parameter is specified,
     * then the data type will allow any currency value. If one is specified, it is expected
     * to be a single, comma-delimited string in which each element is the code for an
     * allowed currency. The code should return a valid currency when passed to
     * {@link java.util.Currency#getInstance(java.lang.String)}.
     * @param ml is the type multi-lingual?
     * @param mv can this type support multiple values?
     * @param req is a value required for this type?
     * @param params the type parameters 
     */
    public DataType<Currency> getType(boolean ml, boolean mv, boolean req, String... params) {
        return getType(null, null, ml, mv, req, params);
    }

    /**
     * Provides a currency data type instance that supports the specified type rules.
     * This data type allows for zero or one type parameters. If no type parameter is specified,
     * then the data type will allow any currency value. If one is specified, it is expected
     * to be a single, comma-delimited string in which each element is the code for an
     * allowed currency. The code should return a valid currency when passed to
     * {@link java.util.Currency#getInstance(java.lang.String)}.
     * @param grp the group of the data type.
     * @param idx the index of the data type.
     * @param ml is the type multi-lingual?
     * @param mv can this type support multiple values?
     * @param req is a value required for this type?
     * @param params the type parameters
     */
    public DataType<Currency> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        if( params == null || params.length < 1 ) {
            return new CurrencyAttribute(grp, idx, ml, mv, req);
        }
        else if( params.length == 1 && (params[0] == null || params[0].length() < 1 || params[0].equals("null")) ) {
            return new CurrencyAttribute(grp, idx, ml, mv, req);
        }
        else {
            return new CurrencyAttribute(grp, idx, ml, mv, req, params[0]);
        }
    }
    
    /**
     * <p>
     *   Implementation of rules for currency attributes.
     * </p>
     * <p>
     *   Last modified: $Date: 2009/01/30 23:01:49 $
     * </p>
     * @version $Revision: 1.6 $
     * @author George Reese
     */
    static public class CurrencyAttribute extends DataType<Currency> {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3689909561478034743L;
        
        /**
         * Options for currency attributes of this type.
         */
        private Collection<Currency> choices  = new ArrayList<Currency>();
        
        /**
         * Constructs a currency attribute that enables users to select among all installed
         * currencies on the system.
         * @param grp the group of the data type.
         * @param idx the index of the data type.
         * @param ml is the attribute multi-lingual?
         * @param mv does the attribute allow multiple values?
         * @param req is a value required for this attribute?
         */
        public CurrencyAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req) {
            super(TYPE_NAME, grp, idx, ml, mv, req, (String[])null);
            for( Locale loc : Locale.getAvailableLocales() ) {
                if( loc.getCountry() == null ) {
                    continue;
                }
                try { choices.add(Currency.getInstance(loc)); }
                catch( IllegalArgumentException sunCanBeRetardedSometimesIgnoreMe ) { }
            }
        }

        /**
         * Constructs a currency attribute instance in which the type parameters define a
         * specific list of allowed currency codes. The codes are specified as a single,
         * comma-delimited string.
         * @param ml is this data type multi-lingual?
         * @param mv does this data type allow multiple values?
         * @param req does this data type require a value?
         * @param codes a comma-delimited string defining acceptable currency codes
         */
        public CurrencyAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req, String codes) {
            super(TYPE_NAME, grp, idx, ml, mv, req, codes);
            String[] parts = codes.split(",");

            if( parts.length < 1 ) {
                parts = new String[1];
                parts[0] = codes;
            }
            for( String code : parts ) {
                choices.add(Currency.getInstance(code));
            }
        }

        /**
         * @return the list of allowed currency values
         */
        public Collection<Currency> getChoices() {
            return choices;
        }
        
        /**
         * @return the factory for currency attributes
         */
        @SuppressWarnings("unchecked")
        public DataTypeFactory<Currency> getFactory() {
            return (DataTypeFactory<Currency>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        /**
         * @return if the data type is multi-valued, {@link org.dasein.attributes.DataType.InputType#MULTI_SELECT}, otherwise
         * {@link org.dasein.attributes.DataType.InputType#SELECT}
         */
        public InputType getInputType() {
            if( isMultiValued() ) {
                return (InputType)InputType.MULTI_SELECT;
            }
            return (InputType)InputType.SELECT;
        }
        
        /**
         * Provides a currency value based on a raw value. Valid raw values include
         * {@link java.util.Currency} instances or currency codes as strings.
         * @param val the raw value
         * @return the currency matching the raw value
         */
        public Currency getValue(Object val) {
            if( val == null ) {
                return null;
            }
            else if( val instanceof Currency ) {
                return (Currency)val;
            }
            else if( val instanceof String ) {
                return Currency.getInstance((String)val);
            }
            else {
                return Currency.getInstance(val.toString());
            }
        }
        
        /**
         * Validates that the specified currency is a valid value for this data type. The
         * specified currency is considered valid if it is among the choices setup
         * for the data type.
         * @param curr the currency being validated
         * @return true if the value is vvalid
         */
        public boolean isValidChoice(Currency curr) {
            if( curr == null ) {
                return !isRequired();
            }
            for( Currency choice : choices ) {
                if( choice.equals(curr) ) {
                    return true;
                }
            }
            return false;
        }
        
        public String toString() {
            return "Currency" + (choices.isEmpty() ? "" : " (constrained)");
        }
    }
}
