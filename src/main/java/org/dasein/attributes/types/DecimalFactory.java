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

package org.dasein.attributes.types;

import java.util.Collection;
import java.util.Locale;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeFactory;
import org.dasein.attributes.InvalidAttributeException;
import org.dasein.util.Translator;

public class DecimalFactory extends DataTypeFactory<Double> {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3545796585259742768L;
    
    static public final String TYPE_NAME = "decimal";
    
    public DecimalFactory() {
        super();
    }

    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    public DataType<Double> getType(boolean ml, boolean mv, boolean req, String... params) {
        return getType(null, null, ml, mv, req, params);
    }

    public DataType<Double> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        return new DecimalAttribute(grp, idx, ml, mv, req);
    }

    static public class DecimalAttribute extends DataType<Double> {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3544675083349342516L;

        public DecimalAttribute(String grp, Number idx,boolean ml, boolean mv, boolean req) {
            super(TYPE_NAME, grp, idx, ml, mv, req, (String[])null);
        }
        
        public Collection<Double> getChoices() {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public DataTypeFactory<Double> getFactory() {
            return (DataTypeFactory<Double>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        public InputType getInputType() {
            return (InputType)InputType.TEXT;
        }
        
        public int getSize() {
            return 10;
        }
        
        public Double getValue(Object src) {
            if( src == null ) {
                return null;
            }
            else if( src instanceof Number ) {
                return ((Number)src).doubleValue();
            }
            else if( src instanceof String ) {
                return Double.valueOf((String)src);
            }
            else {
                throw new InvalidAttributeException("Not a floating point number: " + src);
            }
        }
        
        public boolean isValidChoice(Double num) {
            if( num == null ) {
                return !isRequired();
            }
            return true;
        }
        
        public String toString() {
            return "Decimal";
        }
    }
}
