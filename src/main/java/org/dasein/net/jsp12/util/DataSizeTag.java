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

package org.dasein.net.jsp12.util;

import java.text.NumberFormat;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class DataSizeTag  extends TagSupport {
    private static final long serialVersionUID = -2215155405306714611L;
    
    private Locale locale  = Locale.getDefault();
    private String metric  = null;
    private long   size    = 0L;
    private String var     = null;
    
    public int doEndTag() throws JspException {
        try {
            String str = null;
            
            if( metric != null ) {
                if( metric.equals("k") ) {
                    str = toKilobytes();
                }
                else if( metric.equals("m") ) {
                    str = toMegabytes();
                }
                else if( metric.equals("g") ) {
                    str = toGigabytes();
                }
                else if( metric.equals("t") ) {
                    str = toTerabytes();
                }
            }
            if( str == null ) {
                if( size < 1000000L ) {
                    str = toKilobytes();
                }
                else if( size < 1000000000L ){
                    str = toMegabytes();
                }
                else if( size < 1000000000000L ) {
                    str = toGigabytes();
                }
                else {
                    str = toTerabytes();
                }
            }
            pageContext.setAttribute(var, str);
            return EVAL_PAGE;
        }
        finally {
            size = 0L;
            locale = Locale.getDefault();
            metric = null;
            var = null;
        }
    }
    
    public void setLocale(Locale l) {
        locale = l;
        if( locale == null ) {
            locale = Locale.getDefault();
        }
    }
    
    public void setMetric(String m) {
        metric = m;
        if( metric != null ) {
            if( metric.equals("") ) {
                metric = null;
            }
            else {
                metric = m.toLowerCase().substring(0,1);
                if( !metric.equals("k") && !metric.equals("m") && !metric.equals("g") && !metric.equals("t") ) {
                    metric = null;
                }
            }
        }
    }
    
    public void setSize(Object s) {
    	Number num;
        
        if (s instanceof String) {
        	num = Long.parseLong((String) s);
        } else {
        	num = (Number) s;
        }
        size = num.longValue();
    }
    
    public void setVar(String v) {
        var = v;
    }
    
    private String toGigabytes() {
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(2);
        return fmt.format(((double)size)/1000000000.0) + " GB";        
    }
    
    private String toKilobytes() {
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(2);
        return fmt.format(((double)size)/1000.0) + " KB";        
    }
    
    private String toMegabytes() {
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(2);
        return fmt.format(((double)size)/1000000.0) + " MB";        
    }
    
    private String toTerabytes() {
        NumberFormat fmt = NumberFormat.getNumberInstance(locale);
        
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(2);
        return fmt.format(((double)size)/1000000000000.0) + " TB";        
    }
}
