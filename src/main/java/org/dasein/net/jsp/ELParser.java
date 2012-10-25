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

/* $Id: ELParser.java,v 1.8 2007/10/25 21:51:38 greese Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.dasein.util.CalendarWrapper;

/**
 * <p>
 *   ELParser
 *   TODO Document this class.
 * </p>
 * <p>
 *   Last modified: $Date: 2007/10/25 21:51:38 $
 * </p>
 * @version $Revision: 1.8 $
 * @author george
 */
public class ELParser {
    static private Pattern pattern = Pattern.compile("\\$\\{([\\w\\.]*)\\}");

    private String  attribute = null;
    private boolean dynamic   = false;
    private Matcher matcher   = null;
    
    public ELParser() {
        super();
    }
    
    public ELParser(String attr) {
        super();
        attribute = attr;
        matcher = pattern.matcher(attr);
        dynamic = matcher.matches();
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean getBooleanValue(PageContext ctx) throws JspException {
        Object ob = getValue(ctx);
        
        if( ob == null ) {
            return false;
        }
        if( ob instanceof Boolean ) {
            return ((Boolean)ob).booleanValue();
        }
        if( ob instanceof String ) {
            String str = (String)ob;
            
            return (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes"));
        }
        else if( ob instanceof Number ) {
            long val = ((Number)ob).longValue();
            
            if( val == 0 ) {
                return false;
            }
            else {
                return true;
            }
        }
        throw new JspException("Invalid boolean value: " + ob);
    }
    
    static private final String[] patterns = { 
        "MM/dd/yy", "MM-dd-yy", "MM.dd.yy", "MMM dd, yy", "MM/dd/yyyy", "MM-dd-yyyy", "MM.dd.yyyy", "MMM dd, yyyy"
    };
    
    public Date getDateValue(PageContext ctx) throws JspException {
        Object ob = getValue(ctx);
        
        if( ob == null ) {
            return null;
        }
        if( ob instanceof Number ) {
            return new Date(((Number)ob).longValue());
        }
        if( ob instanceof Date ) {
            return (Date)ob;
        }
        if( ob instanceof Calendar ) {
            return ((Calendar)ob).getTime();
        }
        if( ob instanceof CalendarWrapper ) {
            return ((CalendarWrapper)ob).getDate();
        }
        if( ob instanceof String ) {
            try {
                Long l = Long.valueOf((String)ob);
                
                return new Date(l.longValue());
            }
            catch( NumberFormatException ignore ) {
                // ignore
            }
        }
        if( ob instanceof String ) {
            SimpleDateFormat fmt = new SimpleDateFormat();
            String str = (String)ob;
            Date d = null;
            
            fmt.setLenient(true);
            for( String pattern : patterns ) {
                fmt.applyPattern(pattern);
                try {
                    d = fmt.parse(str);
                }
                catch( ParseException e ) {
                    // ignore
                }
                if( d != null ) {
                    return d;
                }
            }
            throw new JspException("Invalid date format: " + str);
        }
        throw new JspException("Invalid date value: " + ob);
    }
    
    public Number getNumberValue(PageContext ctx) throws JspException {
            Object ob = getValue(ctx);
            
            if( ob == null ) {
                return null;
            }
            else if( ob instanceof Number ) {
                return (Number)ob;
            }
            else if( ob instanceof String ) {
                try {
                    String str = (String)ob;
                    
                 if( str.trim().length() < 1 ) {
                     return null;
                 }
                    return Long.valueOf(str);
                }
                catch( NumberFormatException e ) {
                    throw new JspException("Invalid numeric value: " + ob);
                }
            }
            throw new JspException("Invalid numeric value: " + ob);
    }
    
    public String getName() {
        if( !dynamic ) {
            return null;
        }
        else {
            String nom = matcher.group(1);

            return nom;
        }
    }

    public String getStringValue(PageContext ctx) throws JspException {
            Object ob = getValue(ctx);
            
            if( ob == null ) {
                return null;
            }
            return ob.toString();
    }
    
    public Object getValue(PageContext ctx) throws JspException {
        if( !dynamic ) {
            return attribute;
        }
        else {
            String[] parts = matcher.group(1).split("\\.");
            Object ob = ctx.findAttribute(parts[0]);
            String[] tmp;
            
            if( parts.length == 1 || ob == null ) {
                return ob;
            }
            tmp = new String[parts.length-1];
            for(int i=1; i<parts.length; i++) {
                tmp[i-1] = parts[i];
            }
            return getValue(ob, tmp);
        }
    }
    
    public Object getValue(Object ob, String attr) throws JspException {
        String[] parts;

        if( ob == null ) {
            return null;
        }
        parts = attr.split("\\.");
        if( parts.length < 1 ) {
            parts = new String[1];
            parts[0] = attr;
        }
        return getValue(ob, parts);
    }
    
    public Object getValue(Object ob, String[] parts) throws JspException {
        for(int i=0; i<parts.length; i++) {
            Object[] params;
            Method method;

            method = getMethod(ob, parts[i]);
            if( method == null ) {
                if( ob instanceof Map ) {
                    try {
                        Map<String,?> m = (Map<String,?>)ob;
                    
                        ob = m.get(parts[i]);
                    }
                    catch( ClassCastException e ) {
                        throw new JspException("Could not evaluate " + parts[i] + 
                                " in " + ob.getClass().getName() + ".");
                    }
                }
                else {
                    throw new JspException("Could not evaluate " + parts[i] + 
                            " in " + ob.getClass().getName() + ".");
                }
            }
            else {
                if( ob instanceof Map && method.getName().equals("get") ) {
                    params = new Object[1];
                    params[0] = parts[i];
                }
                else {
                    params = new Object[0];
                }
                try {
                    ob = method.invoke(ob, params);
                }
                catch( InvocationTargetException e ) {
                    Throwable t = e.getCause();

                    System.err.println("Invocation error in " + method.getName() + "/" + ob);
                    e.printStackTrace();
                    if( t != null ) {
                        System.err.println("Embedded: ");
                        t.printStackTrace();
                    }
                    throw new JspException(e.getMessage());                    
                }
                catch( Exception e ) {
                    e.printStackTrace();
                    throw new JspException(e.getMessage());
                }
                if( ob == null ) {
                    break;
                }
            }
        }
        return ob;
    }
    
    private Method getMethod(Object ob, String attr) {
        String tmp = attr.substring(0, 1).toUpperCase();
        Class[] proto = new Class[0];
        String mname;
        
        tmp = tmp + attr.substring(1);
        mname = "get" + tmp;

        try {
            Class cls = ob.getClass();
            Method m = null;
            while (m == null) {
                try {
                    m = cls.getMethod(mname, proto);
                } catch (NoSuchMethodException e) {
                    //ignore
                }
                if (m == null) {
                    cls = cls.getSuperclass();
                    if( cls == null || cls.getName().equals(Object.class.getName()) ) {
                        break;
                    }
                }
            }

            if (m != null) {
                return m;
            }
        }
        catch( Exception ignore ) {
            // ignore me
        }
        mname = "is" + tmp;
        try {

            Class cls = ob.getClass();
            Method m = null;
            while (m == null) {
                try {
                    m = cls.getMethod(mname, proto);
                } catch (NoSuchMethodException e) {
                    //ignore
                }
                if (m == null) {
                    cls = cls.getSuperclass();
                    if( cls == null || cls.getName().equals(Object.class.getName()) ) {
                        break;
                    }
                }
            }

            if (m != null) {
                return m;
            } 
        }
        catch( Exception ignore ) {
            // ignore me
        }
        if( ob instanceof Map ) {
            proto = new Class[1];
            proto[0] = Object.class;
            try {
                return ob.getClass().getMethod("get", proto);
            }
            catch( Exception e ) {
                return null;
            }
        }
        return null;
    }
}
