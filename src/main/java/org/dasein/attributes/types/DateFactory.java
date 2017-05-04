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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeFactory;
import org.dasein.attributes.InvalidAttributeException;
import org.dasein.util.CalendarWrapper;
import org.dasein.util.Translator;

public class DateFactory extends DataTypeFactory<Date> {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257570607120856115L;
    
    static public final String TYPE_NAME = "date";
    
    public Translator<String> getDisplayName() {
        return new Translator<String>(Locale.US, TYPE_NAME);
    }
    
    public String getStringValue(Object ob) {
        if( ob instanceof Date ) {
            return String.valueOf(((Date)ob).getTime());
        }
        return super.getStringValue(ob);
    }
    
    public String getTypeName() {
        return TYPE_NAME;
    }

    public DataType<Date> getType(boolean ml, boolean mv, boolean req, String... typeArgs) {
        return getType(null, null, ml, mv, req, typeArgs);
    }

    public DataType<Date> getType(String grp, Number idx, boolean ml, boolean mv, boolean req, String... typeArgs) {
        String sfmt = null;
        String efmt = null;
        Date start, end;

        if( typeArgs == null || typeArgs.length < 1 ) {
            start = null;
            end = null;
        }
        else if( typeArgs.length == 1 && (typeArgs[0] == null || typeArgs[0].length() < 1) ) {
            start = null;
            end = null;
        }
        else {
            try {
                sfmt = typeArgs[0];
                if( sfmt == null || sfmt.equals(":") || sfmt.equals("") || sfmt.equals("null") ) {
                    sfmt = null;
                    start = null;
                }
                else {
                    start = new Date(Long.valueOf(sfmt));
                }
            }
            catch( NumberFormatException e ) {
                throw new InvalidAttributeException(e.getMessage());
            }
            if( typeArgs.length > 1 ) {
                try {
                    efmt = typeArgs[1];
                    if( efmt == null || efmt.equals("") || efmt.equals("null") ) {
                        efmt = null;
                        end = null;
                    }
                    else {
                        end = new Date(Long.valueOf(efmt));
                    }
                }
                catch( NumberFormatException e) {
                    throw new InvalidAttributeException(e.getMessage());
                }
            }
            else {
                end = null;
            }
        }
        return new DateAttribute(grp, idx, ml, mv, req, start, end, sfmt, efmt);
    }
    
    static public class DateAttribute extends DataType<Date> {
        /**
         * <code>serialVersionUID</code>
         */
        private static final long serialVersionUID = 3257570607120856115L;

        private ArrayList<Date> choices   = null;
        private Date            endDate   = null;
        private Date            startDate = null;
        
        private DateAttribute(String grp, Number idx, boolean ml, boolean mv, boolean req, Date start, Date end, String sfmt, String efmt) {
            super(TYPE_NAME, grp, idx, ml, mv, req, sfmt, efmt);
            if( start != null && end != null ) {
                if( start.getTime() > end.getTime() ) {
                    throw new InvalidAttributeException("Invalid date range: " + start + " - " + end);
                }
            }
            startDate = start;
            endDate = end;
            if( startDate != null && endDate != null ) {
                CalendarWrapper s = new CalendarWrapper();
                CalendarWrapper e = new CalendarWrapper();
                
                s.setDate(start);
                e.setDate(end);
                choices = new ArrayList<Date>();
                while( s.getYear() != e.getYear() || s.getMonth() != e.getMonth() || s.getDayOfMonth() != e.getDayOfMonth() ) {
                    choices.add(s.getDate());
                    s.getCalendar().add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
        
        public Collection<Date> getChoices() {
            return choices;
        }
        
        @SuppressWarnings("unchecked")
        public DataTypeFactory<Date> getFactory() {
            return (DataTypeFactory<Date>)DataTypeFactory.getInstance(TYPE_NAME);
        }
        
        public InputType getInputType() {
            return InputType.DATE;
        }
        
        public Date getValue(Object val) {
            if( val instanceof Date ) {
                return (Date)val;
            }
            else if( val instanceof Long ) {
                return new Date(((Long)val).longValue());
            }
            else if( val instanceof String ) {
                String str = (String)val;
                SimpleDateFormat fmt;

                str = (String)val;
                if( str.equals("") ) {
                    return null;
                }
                fmt = new SimpleDateFormat("yyyy-MM-dd");
                fmt.setLenient(true);
                try {
                    return fmt.parse((String)val);
                }
                catch( ParseException e ) {
                    try {
                        long time = Long.parseLong((String)val);
                        
                        return new Date(time);
                    }
                    catch( NumberFormatException e2 ) {
                        if( ((String)val).indexOf("NONE") == -1 ) {
                            throw new InvalidAttributeException(e.getMessage());
                        }
                        return null;
                    }
                }
            }
            else {
                throw new InvalidAttributeException("Unknown date: "+ val);
            }
        }
        
        public boolean isValidChoice(Date d) {
            if( d == null ) {
                return !isRequired();
            }
            if( startDate == null && endDate == null ) {
                return true;
            }
            if( startDate != null && d.getTime() < startDate.getTime() ) {
                return false;
            }
            if( endDate != null && d.getTime() > endDate.getTime() ) {
                return false;
            }
            return true;
        }
        
        public String toString() {
            return "Date";
        }
    }
}
