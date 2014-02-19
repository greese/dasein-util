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

/* $Id: GroupTag.java,v 1.2 2006/05/03 05:12:49 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.net.jsp.ELParser;

/**
 * <p>
 *   Groups elements into a {@link java.util.Map} of grouped elements in which
 *   the key is a value from a specified field and the values are collections of
 *   items that have the same value for that field.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/05/03 05:12:49 $
 * </p>
 * @version $Revision: 1.2 $
 * @author George Reese
 */
public class GroupTag extends TagSupport {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7144464333794955162L;
    
    private String        attribute  = null;
    private Collection<?> list       = null;
    private String        var        = null;
    private String        varHeaders = null;
    
    @SuppressWarnings("rawtypes")
	public int doEndTag() throws JspException {
        try {
            HashMap<Object,Collection<Object>> map = new HashMap<Object,Collection<Object>>();
            TreeSet<Comparable> headers = new TreeSet<Comparable>();
            ELParser parser = new ELParser();
            
            for( Object ob : list ) {
                Object val = parser.getValue(ob, attribute);
                Collection<Object> tmp;
                
                if( map.containsKey(val) ) {
                    tmp = map.get(val);
                }
                else {
                    tmp = new ArrayList<Object>();
                    map.put(val, tmp);
                    if( varHeaders != null ) {
                        if( val != null ) {
                            if( val instanceof Comparable ) {
                                headers.add((Comparable)val);
                            }
                            else {
                                headers.add(val.toString());
                            }
                        }
                    }
                }
                tmp.add(ob);
            }
            if( varHeaders != null ) {
                pageContext.setAttribute(varHeaders, headers);
            }
            pageContext.setAttribute(var, map);
            return EVAL_PAGE;
        }
        finally {
            attribute = null;
            list = null;
            var = null;
            varHeaders = null;
        }
    }

    public void setAttribute(String attr) {
        attribute = attr;
    }
    
    public void setList(Collection<?> l) {
        list = l;
    }
    
    public void setVar(String v) {
        var = v;
    }
    
    public void setVarHeaders(String hdr) {
        varHeaders = hdr;
    }    
}
