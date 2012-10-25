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

/* $Id: ConstraintTag.java,v 1.1 2006/03/19 16:34:51 greese Exp $ */
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
 *   Last modified: $Date: 2006/03/19 16:34:51 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public class ConstraintTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -286391249683867555L;
    
    private String    type      = null;
    private String    typeInfo  = null;
    private String    var       = null;
    
    public int doEndTag() throws JspException {
        try {
            DataTypeFactory factory = DataTypeFactory.getInstance(type);
            
            if( typeInfo == null ) {
                pageContext.setAttribute(var, factory.getConstraint((String[])null));
            }
            else {
                String[] params = typeInfo.split(",");
                
                if( params == null || params.length < 1 ) {
                    params = new String[1];
                    params[0] = typeInfo;
                }
                pageContext.setAttribute(var, factory.getConstraint(params));
            }
            return EVAL_PAGE;
        }
        finally {
            type = null;
            typeInfo = null;
            var = null;
        }
    }

    public void setType(String t) throws JspException {
        type = (new ELParser(t)).getStringValue(pageContext);
    }
    
    public void setTypeInfo(String val) throws JspException {
        typeInfo = (new ELParser(val)).getStringValue(pageContext);
    }
    
    public void setVar(String v) {
        var = v;
    }
}
