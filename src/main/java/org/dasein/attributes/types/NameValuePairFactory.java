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
import org.dasein.util.NameValuePair;
import org.dasein.util.Translator;

@SuppressWarnings("serial")
public class NameValuePairFactory extends DataTypeFactory<NameValuePair> {
    static public final String TYPE_NAME = "name-value pair";
    
    public NameValuePairFactory() {
        super();
    }
    
    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    public String getStringValue(Object ob) {
        if( ob instanceof NameValuePair ) {
            NameValuePair pair = (NameValuePair)ob;
            String val = pair.getValue();
            
            return (pair.getName().replaceAll("=", "%3D") + "=" + (val == null ? "" : val.replaceAll("=", "%3D")));
        }
        else {
            return super.getStringValue(ob);
        }
    }
    
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    public DataType<NameValuePair> getType(boolean ml, boolean mv, boolean req, String... params) {
        return getType(null, null, ml, mv, req, params);
    }

    public DataType<NameValuePair> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        return new NameValuePairAttribute(grp, idx, ml, mv, req);
    }
    
    static public class NameValuePairAttribute extends DataType<NameValuePair> {

        public NameValuePairAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req) {
            super(TYPE_NAME, grp, idx, ml, mv, req, (String[])null);
        }
        
        public Collection<NameValuePair> getChoices() {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public DataTypeFactory<NameValuePair> getFactory() {
            return (DataTypeFactory<NameValuePair>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        public InputType getInputType() {
            return InputType.PAIR;
        }
        
        public NameValuePair getValue(Object val) {
            String str;
            
            if( val == null ) {
                return null;
            }
            else if( val instanceof String ) {
                str = (String)val;
            }
            else {
                str = val.toString();
            }
            return new NameValuePair(str);
        }
        
        public boolean isValidChoice(NameValuePair pair) {
            return true;
        }
        
        public String toString() {
            return "Name/Value Pair";
        }
    }
    
}
