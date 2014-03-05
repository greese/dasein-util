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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.util.CSVParser;

public class CSVTag extends TagSupport {
    private static final long serialVersionUID = -2239547419306968657L;
    
    private String             fileKey     = null;
    private String             var         = null;
    
    public int doEndTag() throws JspException {
        try {
            String fname = (String)pageContext.findAttribute(fileKey);
            CSVParser parser = new CSVParser(fname);
            
            
            pageContext.setAttribute(var, parser.next());
            return EVAL_PAGE;
        }
        catch( IOException e ) {
            e.printStackTrace();
            throw new JspException(e.getMessage());
        }
        finally {   
            fileKey = null;
            var = null;
        }
    }
    
    public void setFileKey(String fk) {
        fileKey = fk;
    }
    
    public void setVar(String v) {
        var = v;
    }
}
