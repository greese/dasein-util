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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class CollectionTag extends TagSupport {
    private static final long serialVersionUID = 7368053391925940191L;
    
    private Collection<Object> items       = null;
    private Collection<Object> raw         = new ArrayList<Object>(0);
    private boolean            unique      = false;
    private String             var         = null;
    
    public int doEndTag() throws JspException {
        try {
            if( var == null ) {
                if( items != null ) {
                    if( !unique ) {
                        items.addAll(raw);
                    }
                    else {
                        for( Object item : raw ) {
                            if( !items.contains(item) ) {
                                items.add(item);
                            }
                        }
                    }
                }
            }
            else {
                pageContext.setAttribute(var, raw);
            }
            return EVAL_PAGE;
        }
        finally {   
            items = null;
            raw = new ArrayList<Object>(0);
            unique = false;
            var = null;
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setCollection(Collection list)  {
        items = list;
    }
    
    public void setItem(Object item)  {
        raw.add(item);
    }
    
    @SuppressWarnings("unchecked")
    public void setItems( Object ob)  {
        if( ob instanceof Iterable ) {
            Iterable<Object> list = (Iterable<Object>)ob;
            
            for( Object item : list ) {
                raw.add(item);
            }            
        }
        else if( ob instanceof Object[] ) {
            for( Object item : (Object[])ob ) {
                raw.add(item);
            }
        }
    }
    
    public void setUnique(Object u)  {
    	if (u instanceof Boolean) {
    		unique = (Boolean)  u;
    	} else {
    		unique = Boolean.valueOf((String)u);
    	}
    }
    
    public void setVar(String v) {
        var = v;
    }
}
