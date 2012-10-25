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

/* $Id: DataTypeTag.java,v 1.1 2006/02/14 20:03:50 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.attributes.DataTypeFactory;
import org.dasein.net.jsp.ELParser;

/**
 * <p>
 *   Provides JSTL-style access to enum values inside your JSP code. You can access
 *   the full set of values or a specific value.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/02/14 20:03:50 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public class DataTypeTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -286391249683867555L;
    
    private boolean    all       = true;
    private Object     value     = null;
    private String     var       = null;
    
    public int doEndTag() throws JspException {
        try {
            if( value == null && all ) {
                value = DataTypeFactory.getTypes();
            }
            pageContext.setAttribute(var, value);
            return EVAL_PAGE;
        }
        finally {
            all = true;
            value = null;
            var = null;
        }
    }

    public void setTypeName(String val) throws JspException {
        String nom;
        
        all = false;
        nom = (new ELParser(val)).getStringValue(pageContext);
        value = DataTypeFactory.getInstance(nom);
    }
    
    public void setVar(String v) {
        var = v;
    }
}
