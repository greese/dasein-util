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

/* $Id: EscapeTag.java,v 1.1 2007/04/15 11:19:44 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.util;

import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.util.Translator;

/**
 * <p>
 *   Provides JSTL-style access to enum values inside your JSP code. You can access
 *   the full set of values or a specific value.
 * </p>
 * <p>
 *   Last modified: $Date: 2007/04/15 11:19:44 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public class EscapeTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -286391249683867555L;
    
    private Object    value       = null;
    private String    var         = null;
    
    @SuppressWarnings("rawtypes")
	public int doEndTag() throws JspException {
        try {
            if( value instanceof Translator ) {
                Locale loc = pageContext.getRequest().getLocale();
                Translator t = (Translator)value;
                
                if( loc == null ) {
                    loc = Locale.getDefault();
                }
                value = t.getTranslation(pageContext.getRequest().getLocale()).getData();
            }
            if( value instanceof String ) {
                String str = (String)value;
                
                value = str.replaceAll("'", "&#39;");
            }
            pageContext.setAttribute(var, value);
            return EVAL_PAGE;
        }
        finally {
            value = null;
            var = null;
        }
    }

    public void setValue(String itm) {
        value = itm;
    }
    
    public void setVar(String v) {
        var = v;
    }
}
