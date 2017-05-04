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

/* $Id: NameValuePair.java,v 1.1 2006/05/03 05:12:49 greese Exp $ */
/* Copyright (c) 2006 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.io.Serializable;

public class NameValuePair implements Comparable<NameValuePair>, Serializable {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -4071470677974693905L;
    
    private String name  = null;
    private String value = null;
    
    public NameValuePair(String str) {
        super();
        int idx;
        
        idx = str.indexOf("=");
        if( idx == (str.length()-1) ) {
            name = str.substring(0, str.length()-1);
            value = null;
        }
        else if( idx == 0 ) {
            throw new NullPointerException("Illegal name for name value pair: " + str);
        }
        else {
            String[] parts = str.split("=");
            
            name = parts[0];
            if (parts.length > 1) {
            	value = parts[1];
            }
        }
        name = name.replaceAll("%3D", "=");
        if( value != null ) {
            value = value.replaceAll("%3D", "=");
        }
    }
    
    public NameValuePair(String nom, String val) {
        super();
        name = nom.replaceAll("%3D", "=");
        value = val;
        if( value != null ) {
            value = value.replaceAll("%3D", "=");            
        }
        if( name == null ) {
            throw new NullPointerException("The name in a name/value pair cannot be null.");
        }
    }
    
    public int compareTo(NameValuePair other) {
        int x = name.compareTo(other.name);
        
        if( x == 0 ) {
            if( value == null ) {
                if( other.value != null ) {
                    x = -1;
                }
            }
            else if( other.value == null ) {
                x = 1;
            }
            else {
                x = value.compareTo(other.value);
            }
        }
        return x;
    }
    
    public boolean equals(Object ob) {
        NameValuePair other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (NameValuePair)ob;
        if( !name.equals(other.name) ) {
            return false;
        }
        if( value == null ) {
            return (other.name == null);
        }
        return value.equals(other.value);
    }
    
    public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
    
    public String toString() {
        return (name + "=" + value);
    }
}
