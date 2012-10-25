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

package org.dasein.net.jsp.util;

import org.dasein.net.jsp.ELParser;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to store key-value pairs in the JSP.  If just the var attribute is supplied, a new Map is created
 * and set to the incoming var value.  If a varResult, key, and map are provided, the value matching that key is
 * set to the varResult field.  If map, key and value are provided, the key and value are added to the incoming map.  
 * User: james
 * Date: Jan 8, 2008
 * Time: 11:04:41 AM
 */
public class KeyValueTag extends TagSupport {
    private static final long serialVersionUID = 7929968022520009210L;
    
    private Object key;
    private Map map;
    private Object value;
    private String var;
    private String varResult;

    @SuppressWarnings("unchecked")
    public int doEndTag() {

        try {
            
            if (varResult != null && key != null && map != null && value == null) {
                pageContext.setAttribute(varResult, map.get(key));
            } else if (var != null) {
                pageContext.setAttribute(var, new HashMap());
            } else {
                if (map != null && key != null && value != null) {
                    map.put(key, value);
                }
            }

        } finally {
            key = null;
            map = null;
            value = null;
            var = null;
            varResult = null;
        }

        return EVAL_PAGE;
    }

    public void setKey(String str) throws JspException {
        key = new ELParser(str).getValue(pageContext);
    }

    public void setMap(String str) throws JspException {
        map = (Map) new ELParser(str).getValue(pageContext);
    }

    public void setValue(String str) throws JspException {
        value = new ELParser(str).getValue(pageContext);
    }

    public void setVar(String str) throws JspException {
        var = new ELParser(str).getStringValue(pageContext);
    }

    public void setVarResult(String str) throws JspException {
        varResult = new ELParser(str).getStringValue(pageContext);
    }

}
