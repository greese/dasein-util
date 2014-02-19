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

package org.dasein.net.jsp12.date;

import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.util.TimeZoneWrapper;

public class TimeZoneTag  extends TagSupport {
    private static final long serialVersionUID = -6446570452736432042L;
    
    private boolean  all        = true;
    private Locale   locale     = null;
    private String   var        = null;
    private TimeZone zone       = null;
    
    public int doEndTag() throws JspException {
        try {
            if( zone != null || !all ) {
                pageContext.setAttribute(var, new TimeZoneWrapper(zone, locale));
            }
            else {
                pageContext.setAttribute(var, TimeZoneWrapper.getAllZones(locale));
            }
            return EVAL_PAGE;
        }
        finally {
            all = true;
            locale = null;
            var = null;
            zone = null;
        }
    }

    public void setLocale(Locale l) {
        locale = l;
    }
    
    public void setTimeZone(Object ob) {
        all = false;
        if( ob instanceof String ) {
            zone = TimeZone.getTimeZone((String)ob);
        }
        else {
            zone = (TimeZone)ob;
        }
    }
    
    public void setVar(String v) {
        var = v;
    }
}
