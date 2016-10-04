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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeFactory;
import org.dasein.util.Translator;

public class StringFactory extends DataTypeFactory<String> {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4050485612287439156L;
    
    static public final String TYPE_NAME = "string";
    
    private ArrayList<Constraint> constraints = null;
    
    public StringFactory() {
        super();
    }
    
    /**
     * Overrides the default method to allow conditional checking. Only for non-longtext
     * strings do we allow for a list of choices.
     */
    @SuppressWarnings("unchecked")
    public Constraint getConstraint(String ... typeInfo) {
        if( typeInfo == null || typeInfo.length < 1 ) {
            return getConstraints().iterator().next();
        }
        else if( typeInfo.length == 1 ) {
            Constraint c = getConstraint(new String[0]);
            DataType<?> attr = c.getType();
            Boolean b;
            
            b = (Boolean)attr.getValue(typeInfo[0]);
            if( b == null || !b ) {
                return constraints.get(1);
            }
            else {
                return constraints.get(2);
            }
        }
        else {
            return super.getConstraint(typeInfo);
        }
    }
    
    public Collection<Constraint> getConstraints() {
        ArrayList<Constraint> tmp = new ArrayList<Constraint>(2);
        
        if( constraints == null ) {
            ArrayList<Constraint> lt = new ArrayList<Constraint>(2);
            
            lt.add(new Constraint("longtext", DataTypeFactory.getInstance(BooleanFactory.TYPE_NAME).getType(false, false, true), false));
            lt.add(new Constraint("options", DataTypeFactory.getInstance(StringFactory.TYPE_NAME).getType(false,false,false), true));
            constraints = lt;
        }
        tmp.addAll(constraints);
        return tmp;
    }
    
    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    public DataType<String> getType(boolean ml, boolean mv, boolean req, String... params) {
        return getType(null, null, ml, mv, req, params);
    }

    public DataType<String> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        boolean lt;

        if( params == null || params.length < 1 ) {
            return new StringAttribute(grp, idx, ml, mv, req);
        }
        else if( params.length == 1 && params[0] != null && params[0].length() < 1 ) {
            return new StringAttribute(grp, idx, ml, mv, req);
        }
        lt = Boolean.parseBoolean(params[0]);
        if( lt ) {
            return new StringAttribute(grp, idx, ml, mv, req, true);
        }
        if( params.length < 2 || params[1] == null || params[1].equals("null") ) {
            return new StringAttribute(grp, idx, ml, mv, req, false);
        }
        return new StringAttribute(grp, idx, ml, mv, req, params[1]);
    }

    static public class StringAttribute extends DataType<String> {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3258417235385923124L;
        
        private Collection<String> choices  = null;
        private boolean            longtext = false;
        
        public StringAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req) {
            super(TYPE_NAME, grp, idx, ml, mv, req, "false");
        }

        public StringAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req, boolean lt) {
            super(TYPE_NAME, grp, idx, ml, mv, req, String.valueOf(lt), null);
            longtext = lt;
        }

        public StringAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req, String choix) {
            super(TYPE_NAME, grp, idx, ml, mv, req, "false" + (choix == null ? "" : ":" + choix));
            if( choix != null && choix.length() > 0 ) {
                String[] parts = choix.split(",");

                choices = new ArrayList<String>(parts.length);
                if( parts.length < 1 ) {
                    choices.add(choix);
                }
                else {
                    for( String choice : parts ) {
                        choices.add(choice);
                    }
                }
            }
        }

        public Collection<String> getChoices() {
            return choices;
        }
        
        @SuppressWarnings("unchecked")
        public DataTypeFactory<String> getFactory() {
            return (DataTypeFactory<String>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        public InputType getInputType() {
            // the casts here are to get around a retarded Eclipse bug
            if( longtext ) {
                return InputType.TEXTAREA;
            }
            else if( choices != null ) {
                if( isMultiValued() ) {
                    return InputType.MULTI_SELECT;
                }
                return InputType.SELECT;
            }
            else {
                return InputType.TEXT;
            }
        }
        
        public String getValue(Object val) {
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
            return str;
        }
        
        public boolean isLongtext() {
            return longtext;
        }
        
        public boolean isValidChoice(String str) {
            if( choices == null ) {
                return true;
            }
            if( str == null ) {
                return !isRequired();
            }
            for( String choice : choices ) {
                if( choice.equalsIgnoreCase(str) ) {
                    return true;
                }
            }
            return false;
        }
        
        public String toString() {
            if( isLongtext() ) {
                return "Long Text";
            }
            else if( choices == null ) {
                return "Text (free form)";
            }
            else {
                StringBuffer str = new StringBuffer();
                
                str.append("Text");
                if( choices == null || choices.isEmpty() ) {
                    str.append(" (free form)");
                }
                else {
                    str.append(" [");
                    for( String choice : choices ) {
                        str.append(choice);
                        str.append(",");
                    }
                    str.append("]");
                }
                return str.toString();
            }
        }
    }
    
}
