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
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.attributes.DataType;
import org.dasein.attributes.DataTypeMap;

/**
 * Groups a {@link org.dasein.attributes.AttributeMap AttributeMap's} {@link org.dasein.attributes.DataType DataTypes}
 * into collections by the {@link org.dasein.attributes.DataType#group} value.
 * If {@link org.dasein.attributes.DataType#group} is not specified, it goes into the empty group.
 *
 * @author Morgan Catlin <morgan.catlin@valtira.com>
 */
public class GroupDataTypesTag extends TagSupport {
    private static final long serialVersionUID = -7679194103914870813L;
    
    DataTypeMap dataTypes;
    String var;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int doEndTag() throws JspException {
        try  {
            Map<String,Collection<Map>> attrs = new HashMap<String,Collection<Map>>();
            for (Map.Entry e : dataTypes.entrySet()) {
                DataType dt = (DataType) e.getValue();
                String grp = (dt.getGroup() != null ? dt.getGroup() : "");
                if (!attrs.containsKey(grp)) {
                    attrs.put(grp, new ArrayList<Map>());
                }
                Map m = new HashMap();
                m.put(e.getKey(), e.getValue());
                attrs.get(grp).add(m);
            }
            pageContext.setAttribute(var, attrs);
        } finally {
            dataTypes = null;
            var = null;
        }
        return EVAL_PAGE;
    }

    public void setDataTypes(DataTypeMap val) {
        dataTypes = val;
    }

    public void setVar(String v) {
        var = v;
    }
}
