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

/* $Id: TimestampTag.java,v 1.1 2008/02/18 20:06:25 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.net.jsp12.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

public class TimestampTag extends TagSupport { 
    private static final long serialVersionUID = 8387732672937322117L;
    
    private String key = "Timestamp";
    
    public int doEndTag() throws JspException {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
            Date now = new Date();
            
            System.out.println(key + ": " + fmt.format(now));
            return EVAL_PAGE;
        }
        finally {
            key = "Timestamp";
        }
    }

    public void setKey(String key) {
        this.key = key;
    }
}
