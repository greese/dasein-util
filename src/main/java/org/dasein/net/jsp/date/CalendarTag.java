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

/* $Id: CalendarTag.java,v 1.3 2008/12/11 22:24:33 morgan Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.net.jsp.ELParser;
import org.dasein.util.CalendarWrapper;


/**
 * <p>
 *   CalendarTag
 *   TODO Document this class.
 * </p>
 * <p>
 *   Last modified: $Date: 2008/12/11 22:24:33 $
 * </p>
 * @version $Revision: 1.3 $
 * @author george
 */
public class CalendarTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private CalendarWrapper calendar  = null;
    private Date            date      = null;
    private Number          day       = null;
    private Locale          locale    = null;
    private Number          month     = null;
    private Number          time      = null;
    private String          var       = null;
    private Number          year      = null;
    
    public int doEndTag() throws JspException {
        try {
            CalendarWrapper cal;
        
            if( calendar == null ) {
                if( var == null ) {
                    throw new JspException("You must specify a calendar or a var in which to place a new calendar.");
                }
                if( locale == null ) {
                    cal = new CalendarWrapper();
                }
                else {
                    cal = new CalendarWrapper(locale);
                }
            }
            else {
                cal = calendar;
            }
            if( date != null ) {
                cal.setDate(date);
            }
            if( day != null ) {
                cal.getCalendar().set(Calendar.DAY_OF_MONTH, day.intValue());
            }
            if( month != null ) {
                cal.getCalendar().set(Calendar.MONTH, month.intValue()-1);
            }
            if( year != null ) {
                cal.getCalendar().set(Calendar.YEAR, year.intValue());
            }
            if( time != null ) {
                cal.getCalendar().setTimeInMillis(time.longValue());
            }
            if( var != null ) {
                pageContext.setAttribute(var, cal);
            }
            return EVAL_PAGE;
        }
        finally {
            calendar = null;
            date = null;
            day = null;
            locale = null;
            month = null;
            time = null;
            var = null;
            year = null;
        }
    }
    
    public void setCalendar(String cal) throws JspException {
        calendar = (CalendarWrapper)(new ELParser(cal)).getValue(pageContext);
    }
    
    public void setDate(String d) throws JspException {
        date = (new ELParser(d)).getDateValue(pageContext);
    }
    
    public void setDay(String d) throws JspException {
        day = (new ELParser(d)).getNumberValue(pageContext);
    }
    
    public void setLocale(String l) throws JspException {
        locale = (Locale)(new ELParser(l)).getValue(pageContext);
    }
    
    public void setMonth(String m) throws JspException {
        month = (new ELParser(m)).getNumberValue(pageContext);
    }

    public void setTime(String y) throws JspException {
        time = (new ELParser(y)).getNumberValue(pageContext);
    }

    public void setVar(String v) {
        var = v;
    }
    
    public void setYear(String y) throws JspException {
        year = (new ELParser(y)).getNumberValue(pageContext);
    }
}
