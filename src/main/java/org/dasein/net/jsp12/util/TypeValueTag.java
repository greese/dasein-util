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

/* $Id: TypeValueTag.java,v 1.1 2006/03/19 16:34:51 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.util;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.attributes.DataTypeFactory;

/**
 * <p>
 *   Provides JSTL-style access to enum values inside your JSP code. You can access
 *   the full set of values or a specific value.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/03/19 16:34:51 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public class TypeValueTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -286391249683867555L;
    
    private Object    item        = null;
    private Locale    locale      = Locale.getDefault();
    private String    type        = null;
    private String    varDisplay  = null;
    private String    var         = null;
    
    @SuppressWarnings("rawtypes")
	public int doEndTag() throws JspException {
        try {
            DataTypeFactory factory = DataTypeFactory.getInstance(type);

            if( varDisplay != null ) {
                pageContext.setAttribute(varDisplay, factory.getDisplayValue(locale, item));
            }
            pageContext.setAttribute(var, factory.getStringValue(item));
            return EVAL_PAGE;
        }
        finally {
            item = null;
            locale = Locale.getDefault();
            type = null;
            varDisplay = null;
            var = null;
        }
    }

    public void setItem(Object itm) {
        item = itm;
    }
    
    public void setLocale(Locale loc) {
        locale = loc;
    }
    
    public void setType(String t) {
        type = t;
    }
    
    public void setVarDisplay(String v) {
        varDisplay = v;
    }
    
    public void setVar(String v) {
        var = v;
    }
}
