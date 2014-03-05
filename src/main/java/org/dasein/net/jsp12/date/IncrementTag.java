/**
 * Copyright (C) 1998-2012 enStratusNetworks LLC
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

/* $Id: IncrementTag.java,v 1.1 2006/01/12 17:16:45 greese Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.date;

import java.util.Calendar;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.util.CalendarWrapper;


/**
 * <p>
 *   Increments a calendar by the specified values.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/01/12 17:16:45 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public class IncrementTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3257562914801268274L;
    private Calendar calendar  = null;
    private int      increment = 1;
    private int      period    = Calendar.DAY_OF_MONTH;
    
    public int doEndTag() throws JspException {
        try {
            calendar.add(period, increment);
            return EVAL_PAGE;
        }
        finally {
            calendar = null;
            increment = 1;
            period = Calendar.DAY_OF_MONTH;
        }
    }
    
    public void setCalendar(Object ob) throws JspException {
        if( ob == null ) {
            return;
        }
        if( ob instanceof Calendar ) {
            calendar = (Calendar)ob;
        }
        else if( ob instanceof CalendarWrapper ) {
            calendar = ((CalendarWrapper)ob).getCalendar();
        }
        else {
            throw new JspException("You must specify a calendar or calendar wrapper: " + ob);
        }
    }
    
    public void setIncrement(Object inc) {
    	Number num;
        
        if (inc instanceof String) {
        	num = Integer.parseInt((String) inc);
        } else {
        	num = (Number) inc;
        }
        increment = num.intValue();
    }
    
    public void setPeriod(String per) throws JspException {
        per = per.toUpperCase();
        if( per.equals("DAY") ) {
            period = Calendar.DAY_OF_MONTH;
        }
        else if( per.equals("MONTH") ) {
            period = Calendar.MONTH;
        }
        else if( per.equals("YEAR") ) {
            period = Calendar.YEAR;
        }
        else if( per.equals("SECOND") ) {
            period = Calendar.SECOND;
        }
        else if( per.equals("MINUTE") ) {
            period = Calendar.MINUTE;
        }
        else if( per.equals("HOUR") ) {
            period = Calendar.HOUR;
        }
        else {
            throw new JspException("Unknown period: " + per);
        }
    }
}
