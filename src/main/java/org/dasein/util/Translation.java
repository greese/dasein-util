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

/* $Id: Translation.java,v 1.2 2007/04/15 11:19:44 greese Exp $ */
/* Copyright © 2003 George Reese, All Rights Reserved */
package org.dasein.util;

import java.io.Serializable;
import java.util.Locale;

/**
 *
 * <br/>
 * Last modified: $Date: 2007/04/15 11:19:44 $
 * @version $Revision: 1.2 $
 * @author George Reese (http://george.reese.name)
 */
public class Translation<T> implements DelegatedComparable, Serializable {
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3258689922893165368L;
	private T      data    = null;
    private Locale locale  = null;

    public Translation(Locale loc, T dat) {
        super();
        locale = loc;
        data = dat;
    }
    
    public T getData() {
        return data;
    }

    public Object getDelegate(Locale loc) {
        return getData();
    }
    
    public Locale getLocale() {
        return locale;
    }

    public String getString() {
        return data.toString();
    }

    public String toString() {
        if( data == null ) {
            return "<NULL>";
        }
        return data.toString();
    }
}
