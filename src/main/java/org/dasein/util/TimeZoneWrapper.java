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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

public class TimeZoneWrapper implements Serializable {
    private static final long serialVersionUID = -7240957150209843002L;

    static public class ZoneSorter implements Comparator<TimeZone> {
        static private final long offset = CalendarWrapper.HOUR * -12L;
        
        public int compare(TimeZone first, TimeZone second) {
            long r1 = first.getRawOffset();
            long r2 = second.getRawOffset();
            
            if( r1 == r2 ) {
                return first.getID().compareTo(second.getID());
            }
            if( r1 < offset && r2 < offset ) { // both before Hawaii
                return -(new Long(r2)).compareTo(r1);
            }
            else if( r1 >= offset && r2 >= offset ) {
                return -(new Long(r2)).compareTo(r1);
            }
            else if( r1 < offset ) {
                return -1;
            }
            else {
                return 1;
            }
        }
        
    }
    
    static public final Set<TimeZone> timeZones;
    
    static {
        String[] base = { "Pacific/Honolulu", "America/Anchorage", "PST8PDT", "MST7MDT", "CST6CDT",
                "EST5EDT", "Atlantic/Bermuda" };
        TreeSet<TimeZone> sorted = new TreeSet<TimeZone>(new ZoneSorter());
        ArrayList<TimeZone> zones = new ArrayList<TimeZone>();
        
        for( String id : base ) {
            TimeZone tz = TimeZone.getTimeZone(id);
            
            if( tz != null ) {  
                zones.add(tz);
            }
        }
        for( String id : TimeZone.getAvailableIDs() ) {
            TimeZone zone = TimeZone.getTimeZone(id);
            boolean ok = true;
            
            for( TimeZone tz : zones ) {
                if( zone.hasSameRules(tz) ) {
                    ok = false;
                    break;
                }
            }
            if( ok ) {
                zones.add(zone);
            }
        }
        sorted.addAll(zones);
        timeZones = sorted;
    }
    
    static public TimeZoneWrapper[] getAllZones(Locale locale) {
        TimeZoneWrapper[] list = new TimeZoneWrapper[timeZones.size()];
        int i = 0;

        for( TimeZone zone : timeZones ) {
            list[i++] = new TimeZoneWrapper(zone, locale);
        }
        return list;
    }
    
    private Locale   locale   = null;
    private TimeZone timeZone = null;
    
    public TimeZoneWrapper() {
        this(TimeZone.getDefault(), Locale.getDefault());
    }
    
    public TimeZoneWrapper(Locale loc) {
        this(TimeZone.getDefault(), loc);
    }
    
    public TimeZoneWrapper(TimeZone zone) {
        this(zone, Locale.getDefault());
    }

    public TimeZoneWrapper(TimeZone zone, Locale loc) {
        timeZone = zone;
        if( timeZone == null ) {
            timeZone = TimeZone.getDefault();
        }
        locale = loc;
        if( locale == null ) {
            locale = Locale.getDefault();
        }
    }
    
    public long getDstSavings() {
        return timeZone.getDSTSavings();
    }
    
    public String getId() {
        return timeZone.getID();
    }

    public String getLongName() {
        return timeZone.getDisplayName(timeZone.inDaylightTime(new Date()), TimeZone.LONG, locale);
    }
    
    public long getOffset() {
        return timeZone.getOffset(System.currentTimeMillis());
    }
    
    public String getShortName() {
        return timeZone.getDisplayName(timeZone.inDaylightTime(new Date()), TimeZone.SHORT, locale);
    }
    
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    public int hashCode() {
        return timeZone.hashCode();
    }
    
    public boolean isInDaylightTime() {
        return timeZone.inDaylightTime(new Date());
    }
    
    public String toString() {
        return timeZone.getDisplayName(timeZone.inDaylightTime(new Date()), TimeZone.SHORT, locale);
    }
}
