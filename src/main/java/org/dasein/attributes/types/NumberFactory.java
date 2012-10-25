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
import java.util.TreeSet;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeFactory;
import org.dasein.attributes.InvalidAttributeException;
import org.dasein.util.Translator;

@SuppressWarnings("serial")
public class NumberFactory extends DataTypeFactory<Number> {
    static public final String TYPE_NAME = "number";
    
    public NumberFactory() {
        super();
    }

    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    public String getTypeName() {
        return TYPE_NAME;
    }
    
    public DataType<Number> getType(boolean ml, boolean mv, boolean req, String... params) {
        return getType(null, null, ml, mv, req,  params);
    }

    public DataType<Number> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... params) {
        return new NumberAttribute(grp, idx, ml, mv, req);
    }
    
    static public class NumberAttribute extends DataType<Number> {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3976740285046733107L;

        static private class Range {
            public Number start;
            public Number end;
            public int    increment;
            
            public Range(Number s, Number e, int inc) {
                super();
                start = s;
                end = e;
                increment = inc;
            }
        }

        private Collection<Number> numbers   = null;
        private Collection<Range>  ranges    = null;
        
        public NumberAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req) {
            super(TYPE_NAME, grp, idx, ml, mv, req, (String[])null);
        }
        
        public NumberAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req, String rng) {
            super(TYPE_NAME, grp, idx, ml, mv, req, rng);
            parseRange(rng, 1);
        }
        
        public NumberAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req, String rng, String inc) {
            super(TYPE_NAME, grp, idx, ml, mv, req, rng, inc);
            parseRange(rng, Integer.parseInt(inc));
        }
        
        public Collection<Number> getChoices() {
            return numbers;
        }
        
        @SuppressWarnings("unchecked")
        public DataTypeFactory<Number> getFactory() {
            return (DataTypeFactory<Number>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        public InputType getInputType() {
            if( numbers == null ) {
                return InputType.TEXT;
            }
            if( numbers.size() > 100 ) {
                numbers = null;
                return InputType.TEXT;
            }
            if( isMultiValued() ) {
                return InputType.MULTI_SELECT;
            }
            return InputType.SELECT;
        }
        
        public int getSize() {
            return 10;
        }
        
        public Number getValue(Object src) {
            if( src == null ) {
                return null;
            }
            else if( src instanceof Number ) {
                return (Number)src;
            }
            else if( src instanceof String ) {
                String str = (String)src;
                
                if( str.trim().equals("") ) {
                    return null;
                }
                return Long.valueOf((String)src);
            }
            else {
                throw new InvalidAttributeException("Not a number: " + src);
            }
        }
    
        private void parseRange(String range, int inc) {
            String[] parts;
            
            ranges = new ArrayList<Range>();
            numbers = new TreeSet<Number>();
            if( range == null ) {
                return;
            }
            parts = range.split(",");
            if( parts.length < 1 ) {
                parts = new String[1];
                parts[0] = range;
            }
            for( String part : parts ) {
                int idx = part.indexOf("-");
                
                if( idx == 0 ) {
                    idx = part.lastIndexOf("-");
                    if( idx == 0 ) {
                        idx = -1;
                    }
                }
                if( idx == -1 ) {
                    Number num = Long.valueOf(part);
                    
                    if( numbers != null ) {
                        numbers.add(num.longValue());
                    }
                    ranges.add(new Range(num, num, inc));
                }
                else {
                    Number start, end;
                    String s, e;
                    long diff;
                    Range r;
                    int i;
                    
                    s = part.substring(0,idx);
                    i = s.lastIndexOf("-");
                    if( i > 0 ) {
                        idx = i;
                        s = part.substring(0,idx);
                    }
                    e = part.substring(idx+1);
                    start = Long.valueOf(s);
                    end = Long.valueOf(e);
                    diff = end.longValue() - start.longValue();
                    if( diff < 0 ) {
                        Number num = end;
                        
                        end = start;
                        start = num;
                        diff = end.longValue() - start.longValue();
                    }
                    if( (diff/inc) > 30 ) {
                        numbers = null;
                    }
                    r = new Range(start, end, inc);
                    ranges.add(r);
                    if( numbers != null ) {
                        for(long val=start.longValue(); val<=end.longValue(); val++) {
                            numbers.add(val);
                        }
                    }
                }
            }
        }
        
        public boolean isValidChoice(Number num) {
            if( num == null ) {
                return !isRequired();
            }
            if( numbers != null ) {
                return numbers.contains(num.longValue());
            }
            for( Range rng : ranges ) {
                if( rng.start.longValue() > num.longValue() ) {
                    continue;
                }
                if( rng.end.longValue() < num.longValue() ) {
                    continue;
                }
                if( rng.increment == 1 ) {
                    return true;
                }
                if( rng.increment == 0 ) {
                    continue;
                }
                for( long i=rng.start.longValue(); i<rng.end.longValue(); i+=rng.increment ) {
                    if( i == num.longValue() ) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        public String toString() {
            return "Number";
        }
    }
}
