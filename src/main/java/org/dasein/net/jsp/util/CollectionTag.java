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

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.net.jsp.ELParser;

public class CollectionTag extends TagSupport {
    private static final long serialVersionUID = 7368053391925940191L;
    
    private Collection<Object> items       = null;
    private Collection<Object> raw         = new ArrayList<Object>();
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
            raw = new ArrayList<Object>();
            unique = false;
            var = null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public void setCollection(String list) throws JspException {
        items = (Collection<Object>)(new ELParser(list)).getValue(pageContext);
    }
    
    public void setItem(String item) throws JspException {
        Object ob = (new ELParser(item)).getValue(pageContext);
        
        raw.add(ob);
    }
    
    @SuppressWarnings("unchecked")
    public void setItems(String items) throws JspException {
        Object ob = (new ELParser(items)).getValue(pageContext);
        
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
    
    public void setUnique(String u) throws JspException {
        unique = (new ELParser(u)).getBooleanValue(pageContext);
    }
    
    public void setVar(String v) {
        var = v;
    }
}
