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

package org.dasein.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public enum DayOfWeek {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
    
    static public EnumSet<DayOfWeek> fromString(String days) {
        String[] parts = days.split(",");
        
        if( parts == null || parts.length < 1 ) {
            parts = new String[] { days };
        }
        ArrayList<DayOfWeek> list = new ArrayList<DayOfWeek>();
        
        for( String day : parts ) {
            list.add(DayOfWeek.valueOf(day));
        }
        return EnumSet.copyOf(list);
    }
    
    static public String fromEnumSet(EnumSet<DayOfWeek> days) {
        StringBuilder str = new StringBuilder();
        boolean first = true;
        
        for( DayOfWeek day : days ) {
            if( !first ) {
                str.append(",");
            }
            first = false;
            str.append(day.name());
        }
        return str.toString();
    }
    
    static public DayOfWeek getInstance() {
        return getInstance(Calendar.getInstance());
    }
    
    static public DayOfWeek getInstance(int calendarDayOfWeek) {
        switch( calendarDayOfWeek ) {
            case Calendar.SUNDAY: return SUNDAY;
            case Calendar.MONDAY: return MONDAY;
            case Calendar.TUESDAY: return TUESDAY;
            case Calendar.WEDNESDAY: return WEDNESDAY;
            case Calendar.THURSDAY: return THURSDAY;
            case Calendar.FRIDAY: return FRIDAY;
            case Calendar.SATURDAY: return SATURDAY;
        }
        return null;
    }
    
    static public DayOfWeek getInstance(long when) {
        return getInstance(new Date(when));
    }
    
    static public DayOfWeek getInstance(Date date) {
        Calendar cal = Calendar.getInstance();
        
        cal.setTime(date);
        return getInstance(cal);
    }
    
    static public DayOfWeek getInstance(Calendar calendar) {
        return getInstance(calendar.get(Calendar.DAY_OF_WEEK));        
    }
    
    static public DayOfWeek getInstance(CalendarWrapper when) {
        return getInstance(when.getDayOfWeek());
    }
    
    public int getCalendarDay() {
        switch( this ) {
            case SUNDAY: return Calendar.SUNDAY;
            case MONDAY: return Calendar.MONDAY;
            case TUESDAY: return Calendar.TUESDAY;
            case WEDNESDAY: return Calendar.WEDNESDAY;
            case THURSDAY: return Calendar.THURSDAY;
            case FRIDAY: return Calendar.FRIDAY;
            case SATURDAY: return Calendar.SATURDAY;
        }
        return 8;
    }

    
    public List<DayOfWeek> getNextWeek() {
        return getNextWeek(true);
    }
    
    public List<DayOfWeek> getNextWeek(boolean startingToday) {
        if( !startingToday ) {
            return getTomorrow().getNextWeek(true);
        }
        ArrayList<DayOfWeek> week = new ArrayList<DayOfWeek>();
        
        week.add(this);
        for( DayOfWeek day : DayOfWeek.values() ) {
            if( day.isAfter(this) ) {
                week.add(day);
            }
        }
        for( DayOfWeek day : DayOfWeek.values() ) {
            if( day.isBefore(this) ) {
                week.add(day);
            }
        }
        return week;
    }
    
    public DayOfWeek getTomorrow() {
        if( equals(DayOfWeek.SATURDAY) ) {
            return DayOfWeek.SUNDAY;
        }
        return DayOfWeek.getInstance(getCalendarDay()+1);
    }
    
    public DayOfWeek getYesterday() {
        if( equals(DayOfWeek.SUNDAY) ) {
            return DayOfWeek.SATURDAY;
        }
        return getInstance(getCalendarDay()-1);
    }
    
    public boolean isAfter(DayOfWeek dayOfWeek) {
        return (dayOfWeek.getCalendarDay() < getCalendarDay());
    }
    
    public boolean isBefore(DayOfWeek dayOfWeek) {
        return (dayOfWeek.getCalendarDay() > getCalendarDay());
    }
    
    public String toString() {
        return toString(Locale.getDefault());
    }
    
    public String toString(Locale locale) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("EEEE", locale);
            Calendar cal = Calendar.getInstance();
            
            cal.set(Calendar.DAY_OF_WEEK, getCalendarDay());
            return fmt.format(cal.getTime());
        }
        catch( Throwable ignore ) {
            return super.toString();
        }
    }
}
