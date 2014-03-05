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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.net.jsp.ELParser;

public class MapTag extends TagSupport {
    private static final long serialVersionUID = -6096962677598380109L;
    
    private Collection<Object> items       = null;
    private String             key         = null;
    private String             var         = null;
    private String             varKeys     = null;
    
    public int doEndTag() throws JspException {
        try {
            HashMap<Object,Collection<Object>> map = new HashMap<Object,Collection<Object>>();
            ELParser parser = new ELParser();
            TreeSet<Object> keys;
            
            if( varKeys != null ) {
                keys = new TreeSet<Object>();
            }
            else {
                keys = null;
            }
            for( Object item : items ) {
                Object k = parser.getValue(item, key);
                Collection<Object> tmp;
                
                if( !map.containsKey(k) ) {
                    tmp = new ArrayList<Object>();
                    map.put(k, tmp);
                }
                else {
                    tmp = map.get(k);
                }
                tmp.add(item);
                if( varKeys != null && k != null ) {
                    keys.add(k);
                }
            }
            pageContext.setAttribute(var, map);
            if( varKeys != null ) {
                pageContext.setAttribute(varKeys, keys);
            }
            return EVAL_PAGE;
        }
        finally {   
            items = null;
            key = null;
            var = null;
            varKeys = null;
        }
    }
    
    public void setKey(String k) {
        key = k;
    }
    
    public void setItems(Collection<Object> it) {
        items = it;
    }
    
    public void setVar(String v) {
        var = v;
    }
    
    public void setVarKeys(String v) {
        varKeys = v;
    }
}
