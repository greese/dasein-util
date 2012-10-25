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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class TimeWindow implements Iterable<TimePeriod> {
    static public TimeWindow valueOf(String value) {
        String[] parts = value.split("/");
        
        if( parts.length != 3 ) {
            throw new DateFormatException("Invalid time window format " + value);
        }
        int startHour, startMinute, endHour, endMinute;
        
        String[] start = parts[1].split(":");
        String[] end = parts[2].split(":");
        
        startHour = Integer.parseInt(start[0]);
        startMinute = Integer.parseInt(start[1]);
        endHour = Integer.parseInt(end[0]);
        endMinute = Integer.parseInt(end[1]);
        return new TimeWindow(DayOfWeek.fromString(parts[0]), startHour, startMinute, endHour, endMinute);
    }
    
    private EnumSet<DayOfWeek> daysOfWeek   = EnumSet.of(DayOfWeek.SUNDAY);
    private int                endHour      = 1;
    private int                endMinute    = 0;
    private boolean            spansDays    = false;
    private int                startHour    = 0;
    private int                startMinute  = 0;
    
    public TimeWindow() { }
    
    public TimeWindow(EnumSet<DayOfWeek> days, int startHour, int startMinute, int endHour, int endMinute) {
        if( startHour > endHour ) {
            spansDays = true;
        }
        else if( startHour == endHour && startMinute > endMinute ) {
            spansDays = true;
        }
        daysOfWeek = EnumSet.copyOf(days);
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }
    
    public EnumSet<DayOfWeek> getDaysOfWeek() {
        return EnumSet.copyOf(daysOfWeek);
    }
    
    public long getDuration() {
        int dh, dm;
        
        if( spansDays ) {
            if( endMinute >= startMinute ) {
                dh = (24 + endHour) - startHour;
                dm = endMinute - startMinute;
            }
            else {
                dh = (23 + endHour) - startHour;
                dm = (60 + endMinute) - startMinute;
            }
        }
        else {
            if( endMinute >= startMinute ) {
                dh = endHour - startHour;
                dm = endMinute - startMinute;
            }
            else {
                dh = (endHour-1) - startHour;
                dm = (60 + endMinute) - startMinute;
            }
        }
        return (dh * CalendarWrapper.HOUR) + (dm * CalendarWrapper.MINUTE);
    }
    
    public int getEndHour() {
        return endHour;
    }
    
    public int getEndMinute() {
        return endMinute;
    }
    
    public Iterable<TimePeriod> getNextPeriods(int numPeriods) {
        return getNextPeriods(System.currentTimeMillis(), numPeriods);
    }
    
    public Iterable<TimePeriod> getNextPeriods(long startingTimestamp, int numPeriods) {
        ArrayList<TimePeriod> periods = new ArrayList<TimePeriod>();
        Calendar calendar = Calendar.getInstance();
        
        calendar.setTimeInMillis(startingTimestamp);
        
        while( periods.size() < numPeriods ) {
            DayOfWeek today = DayOfWeek.getInstance(calendar);
            int hour = calendar.get(Calendar.HOUR);
            int minute = calendar.get(Calendar.MINUTE);
            int durationInHours, durationInMinutes;
            DayOfWeek startDay;

            calendar.set(Calendar.HOUR, startHour);
            calendar.set(Calendar.MINUTE, startMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            if( spansDays ) {
                if( endMinute >= startMinute ) {
                    durationInHours = (24 + endHour) - startHour;
                    durationInMinutes = endMinute - startMinute;
                }
                else {
                    durationInHours = (23 + endHour) - startHour;
                    durationInMinutes = (60 + endMinute) - startMinute;
                }
                if( (hour < endHour) || (hour == endHour && minute < endMinute) ) {
                    startDay = today.getYesterday();
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                }
                else {
                    startDay = today;
                }
            }
            else {
                if( endMinute >= startMinute ) {
                    durationInHours = endHour - startHour;
                    durationInMinutes = endMinute - startMinute;
                }
                else {
                    durationInHours = (endHour-1) - startHour;
                    durationInMinutes = (60 + endMinute) - startMinute;
                }
                if( (hour < endHour) || (hour == endHour && minute < endMinute) ) {
                    startDay = today;
                }
                else {
                    startDay = today.getTomorrow();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
            for( DayOfWeek dayOfWeek : startDay.getNextWeek() ) {
                if( daysOfWeek.contains(dayOfWeek) ) {
                    periods.add(new TimePeriod(calendar.getTimeInMillis(), durationInHours, durationInMinutes));
                }
                calendar.add(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
            }
        }
        return periods;
    }
    
    private String getString(int x) {
        if( x < 10 ) {
            return "0" + x;
        }
        return String.valueOf(x);
    }
    
    public int getStartHour() {
        return startHour;
    }
    
    public int getStartMinute() {
        return startMinute;
    }
    
    public boolean isInWindow() {
        return isInWindow(System.currentTimeMillis());
    }
    
    public boolean isInWindow(long atTimestamp) {
        if( daysOfWeek.size() < 1 ) {
            return false;
        }
        CalendarWrapper when = new CalendarWrapper();
        int start, end, current;

        when.setDate(new Date(atTimestamp));
        if( endHour > startHour || (endHour == startHour && endMinute >= startMinute) ) {
            boolean today = false;

            for( DayOfWeek day : getDaysOfWeek() ) {
                if( when.getDayOfWeek() == day.getCalendarDay() ) {
                    today = true;
                    break;
                }
            }
            if( !today ) {
                return false;
            }
            start = (startHour * 60) + startMinute;
            end = (endHour * 60) + endMinute;
            current = (when.getHour() * 60) + when.getMinute();
            return (current >= start && current < end);
        }
        else {
            if( startMinute > endMinute ) {
                end = (endHour * 60) + (60 + endMinute);
            }
            else {
                end = (endHour * 60) + endMinute;
            }
            start = (startHour * 60) + startMinute;
            current = (when.getHour() * 60) + when.getMinute();
            if( current >= start ) {
                boolean today = false;
                
                for( DayOfWeek day : getDaysOfWeek() ) {
                    if( when.getDayOfWeek() == day.getCalendarDay() ) {
                        today = true;
                        break;
                    }
                }      
                return today;
            }
            else if( current < end ){
                boolean yesterday = false;
                
                for( DayOfWeek day : getDaysOfWeek() ) {
                    day = day.getYesterday();
                    if( when.getDayOfWeek() == day.getCalendarDay() ) {
                        yesterday = true;
                        break;
                    }
                }             
                return yesterday;
            }
            return false;
        }
    }
    

    public Iterator<TimePeriod> iterator() {
        int size = daysOfWeek.size();
        
        if( size < 1 ) {
            List<TimePeriod> empty = Collections.emptyList();
            
            return empty.iterator();
        }
        return getNextPeriods(size).iterator();
    }
    
    public String toString() {
        return DayOfWeek.fromEnumSet(daysOfWeek) + "/" + getString(startHour) + ":" + getString(startMinute) + "/" + getString(endHour) + ":" + getString(endMinute);
    }
}
