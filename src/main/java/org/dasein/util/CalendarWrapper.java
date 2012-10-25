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

/* $Id: CalendarWrapper.java,v 1.9 2006/12/05 16:58:44 morgan Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * <p>
 *   Creates sanity out of the utter crap known as the {@link java.util.Calendar} class by
 *   wrapping common methods in bean-friendly calls.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/12/05 16:58:44 $
 * </p>
 * @version $Revision: 1.9 $
 * @author George Reese
 */
public class CalendarWrapper implements Serializable {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4049359716691882545L;

    static public final long SECOND    = 1000L;
    static public final long MINUTE    = (SECOND * 60L);
    static public final long HOUR      = (MINUTE * 60L);
    static public final long DAY       = (HOUR * 24L);
    static public final long WEEK      = (DAY * 7L);
    static public final long MONTH     = (DAY * 30L);
    static public final long YEAR      = (DAY * 365L);
    static public final long DECADE    = (YEAR * 10L) + (DAY * 2L);
    static public final long CENTURY   = (YEAR * 100L) + (DAY * 25L);
    static public final long MILLENIUM = (CENTURY * 10L) - (DAY * 2L);

    public Calendar calendar = null;

    public CalendarWrapper() {
        super();
        calendar = Calendar.getInstance();
    }

    public CalendarWrapper(Locale loc) {
        super();
        calendar = Calendar.getInstance(loc);
    }

    public CalendarWrapper(Calendar cal) {
        super();
        calendar = cal;
    }

    public synchronized Calendar getCalendar() {
        return calendar;
    }

    public synchronized Date getDate() {
        return calendar.getTime();
    }

    public synchronized int getDaysFrom(long then) {
        CalendarWrapper cal = new CalendarWrapper();

        cal.setDate(new Date(then));
        return getDaysFrom(cal);
    }

    public synchronized int getDaysFrom(Date then) {
        CalendarWrapper cal = new CalendarWrapper();

        cal.setDate(then);
        return getDaysFrom(cal);
    }

    public synchronized int getDaysFrom(CalendarWrapper then) {
        long now, tt;

        now = getMidnight().getTime();
        tt = then.getMidnight().getTime();

        // add an hour if the dates span the daylight savings windows
        if (getMidnight().getCalendar().getTimeZone().inDaylightTime(getMidnight().getDate())) {
            now += HOUR;
        }
        if (then.getMidnight().getCalendar().getTimeZone().inDaylightTime(then.getMidnight().getDate())) {
            tt += HOUR;
        }
        return (int)((now-tt)/DAY);
    }

    public synchronized int getDaysFromNow() {
        return getDaysFrom(new CalendarWrapper());
    }

    public synchronized int getHoursFrom(long then) {
        CalendarWrapper cal = new CalendarWrapper();

        cal.setDate(new Date(then));
        return getHoursFrom(cal);
    }

    public synchronized int getHoursFrom(Date then) {
        CalendarWrapper cal = new CalendarWrapper();

        cal.setDate(then);
        return getHoursFrom(cal);
    }

    public synchronized int getHoursFrom(CalendarWrapper then) {
        long now, tt;

        now = getTime();
        tt = then.getTime();
        return (int)((now-tt)/HOUR);
    }

    public synchronized int getYearsFrom(CalendarWrapper then) {
        long now, tt;

        now = getMidnight().getTime();
        tt = then.getMidnight().getTime();
        return (int)((now-tt)/YEAR);
    }

    public synchronized int getYearsFromNow() {
        return getYearsFrom(new CalendarWrapper());
    }

    public synchronized int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public synchronized int getDayOfWeek() {
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public synchronized int getDaysInMonth() {
        Date d = calendar.getTime();

        try {
            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            return getDayOfMonth();
        }
        finally {
            calendar.setTime(d);
        }
    }

    public synchronized int getHour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public synchronized CalendarWrapper getMidnight() {
        CalendarWrapper tmp = new CalendarWrapper();

        tmp.setDate(getDate());
        tmp.getCalendar().set(Calendar.HOUR_OF_DAY, 0);
        tmp.getCalendar().set(Calendar.MINUTE, 0);
        tmp.getCalendar().set(Calendar.SECOND, 0);
        tmp.getCalendar().set(Calendar.MILLISECOND, 0);
        return tmp;
    }

    public synchronized int getMillisecond() {
        return calendar.get(Calendar.MILLISECOND);
    }

    public synchronized int getMinute() {
        return calendar.get(Calendar.MINUTE);
    }

    public synchronized int getMonth() {
        // why does Java do months as 0 based????
        return calendar.get(Calendar.MONTH) + 1;
    }

    public synchronized int getSecond() {
        return calendar.get(Calendar.SECOND);
    }

    public synchronized long getTime() {
        return calendar.getTimeInMillis();
    }

    public synchronized CalendarWrapper getWeekBegin() {
    	CalendarWrapper tmp = new CalendarWrapper();
    	tmp.setDate(getDate());
    	int currentDOW = tmp.getDayOfWeek();
    	tmp.getCalendar().add(Calendar.DAY_OF_YEAR, (currentDOW * -1)+1);
    	return tmp;
    }

    public synchronized int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    public synchronized void setDate(Date d) {
        calendar.setTime(d);
    }

    public synchronized boolean isLeapYear() {
        Date d = calendar.getTime();

        try {
            calendar.set(Calendar.MONTH, 2);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            if( getDayOfMonth() == 29 ) {
                return true;
            }
            return false;
        }
        finally {
            calendar.setTime(d);
        }
    }

    public synchronized boolean isWeekend() {
        int dow = getDayOfWeek();

        return (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
    }

    public synchronized String toString() {
        return calendar.getTime().toString();
    }
}
