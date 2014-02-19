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

/* $Id: EnumTag.java,v 1.3 2007/03/25 18:45:44 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>
 *   Provides JSTL-style access to enum values inside your JSP code. You can access
 *   the full set of values or a specific value.
 * </p>
 * <p>
 *   Last modified: $Date: 2007/03/25 18:45:44 $
 * </p>
 * @version $Revision: 1.3 $
 * @author George Reese
 */
public class EnumTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3256999943456830516L;
    
    private boolean                 all       = true;
    private Class<? extends Enum>   enumClass = null;
    private String                  value     = null;
    private String                  var       = null;
    
    @SuppressWarnings("unchecked")
    public int doEndTag() throws JspException {
        try {
            Object result;
            
            if( enumClass == null ) {
                throw new JspException("You must provide a value for enumName.");
            }
            if( value != null ) {
                result = Enum.valueOf(enumClass, value);
            }
            else if( all ) {
                Method meth = enumClass.getDeclaredMethod("values", new Class[0]);
                
                result = meth.invoke(null, new Object[0]);
            }
            else {
                result = null;
            }
            pageContext.setAttribute(var, result);
            return EVAL_PAGE;
        }
        catch( SecurityException e ) {
            throw new JspException(e.getMessage());
        }
        catch( NoSuchMethodException e ) {
            throw new JspException(e.getMessage());
        }
        catch( IllegalArgumentException e ) {
            throw new JspException(e.getMessage());
        }
        catch( IllegalAccessException e ) {
            throw new JspException(e.getMessage());
        }
        catch( InvocationTargetException e ) {
            throw new JspException(e.getMessage());
        }
        finally {
            all = true;
            enumClass = null;
            value = null;
            var = null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setEnumName(String nom) throws ClassNotFoundException {
        if( nom != null ) {
            try {
                enumClass = (Class<? extends Enum>)Class.forName(nom);
            }
            catch( ClassNotFoundException e ) {
                int idx = nom.lastIndexOf(".");
                String tmp;

                if( idx == -1 ) {
                    throw e;
                }
                tmp = nom.substring(0, idx);
                nom = nom.substring(idx+1);
                try {
                    enumClass = (Class<? extends Enum>)Class.forName(tmp + "$" + nom);
                }
                catch( ClassNotFoundException e2 ) {
                    Class cls = Class.forName(tmp);
                    
                    for( Class child : cls.getClasses() ) {
                        if( child.getName().endsWith(nom) ) {
                            enumClass = (Class<? extends Enum>)child;
                            break;
                        }
                    }
                    if( enumClass == null ) {
                        throw e;
                    }
                }
            }
        }
    }
    
    public void setValue(String val) {
        all = false;
        value = val;
    }
    
    public void setVar(String v) {
        var = v;
    }
}
