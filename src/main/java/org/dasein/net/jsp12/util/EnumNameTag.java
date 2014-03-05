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

/* $Id: EnumNameTag.java,v 1.3 2007/03/24 14:49:53 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * <p>
 *   The geniuses at Sun made it so you cannot access the {@link Enum#name()} value from
 *   JSTL. This tag fixes that problem.
 * </p>
 * <p>
 *   Last modified: $Date: 2007/03/24 14:49:53 $
 * </p>
 * @version $Revision: 1.3 $
 * @author George Reese
 */
public class EnumNameTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3546360651887816759L;
    
    @SuppressWarnings("rawtypes")
	private Enum   value = null;
    private String var       = null;
    
    public int doEndTag() throws JspException {
        try {
            pageContext.setAttribute(var, value.name());
            return EVAL_PAGE;
        }
        finally {
            value = null;
            var = null;
        }
    }

    
    @SuppressWarnings("rawtypes")
	public void setEnum(Enum val) {
        value = val;
    }
    
    public void setVar(String v) {
        var = v;
    }
}
