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

package org.dasein.util;

import java.lang.reflect.Method;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class GenericSorter<T> implements Comparator<T> {
    static private class CriteriaMethod<T> {
        private String           chain      = null;
        private Method           method     = null;
        private Object[]         parameters = null;
        private GenericSorter<T> sorter     = null;

        public CriteriaMethod(Method m, GenericSorter<T> s) {
            this(m, s, null);
        }
        
        public CriteriaMethod(Method m, GenericSorter<T> s, Locale l) {
            super();
            method = m;
            sorter = s;
            if( l == null ) {
                parameters = new Object[0];
            }
            else {
                parameters = new Object[1];
                parameters[0] = l;
            }
        }
            
        public Object invoke(Object targ) throws Exception {
            targ = method.invoke(targ, parameters);
            if( chain == null ) {
                return targ;
            }
            return sorter.getMethod(targ, chain).invoke(targ); 
        }
        
        public String toString() {
            return (method + " (" + parameters + "): " + chain);
        }
    }
    
    private String[] criteria   = null;
    private boolean  descending = false;
    private Locale   locale     = null;

    public GenericSorter(String crit) {
        this(crit, Locale.getDefault(), false);
    }

    public GenericSorter(String crit, Locale loc) {
        this(crit, loc, false);
    }

    public GenericSorter(String crit, Locale loc, boolean desc) {
        super();
        if( crit == null ) {
            criteria = new String[0];
        }
        else {
            criteria = crit.split(",");
        }
        if( loc == null ) {
        		loc = Locale.getDefault();
        }
        locale = loc;
        descending = desc;
    }

    public int compare(T one, T two) {
        if( one == null ) {
            if( two == null ) {
                return 0;
            }
            else if( descending ) {
                return 1;
            }
            else {
                return -1;
            }
        }
        else if( two == null ) {
            if( descending ) {
                return -1;
            }
            else {
                return 1;
            }
        }
        if( criteria.length < 1 ) {
            return sort(getValue(one), getValue(two));
        }
        for(int i=0; i<criteria.length; i++) {
            CriteriaMethod<T> m1 = getMethod(one, criteria[i]);
            CriteriaMethod<T> m2 = getMethod(two, criteria[i]);
            Object v1, v2;

            try {
                int x;

                if( m1 == null ) {
                    if( one instanceof String ) {
                        v1 = (String)one;
                    }
                    else if( one instanceof Number ) {
                        v1 = (Number)one;
                    }
                    else if( one instanceof Boolean ) {
                        v1 = (Boolean)one;
                    }
                    else {
                        v1 = null;
                    }
                }
                else {
                    v1 = m1.invoke(one);
                }
                if( m2 == null ) {
                    if( two instanceof String ) {
                        v2 = (String)two;
                    }
                    else if( two instanceof Number ) {
                        v2 = (Number)two;
                    }
                    else if( two instanceof Boolean ) {
                        v2 = (Boolean)two;
                    }
                    else {
                        v2 = null;
                    }
                }
                else {
                    v2 = m2.invoke(two);
                }
                x = sort(getValue(v1), getValue(v2));
                if( x != 0 ) {
                    return x;
                }
            }
            catch( Exception e ) {
                e.printStackTrace();
                throw new RuntimeException("Unable to pull sort values.");
            }
        }
        return sort(getValue(one), getValue(two));
    }
    
    public String[] getCriteria() {
        return criteria;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    private CriteriaMethod<T> getMethod(Object ob, String attr) {
        Class[] lparams = { Locale.class };
        Class[] params = new Class[0];
        String mname;
        Method m;
        
        if( attr.indexOf(".") != -1 ) {
            int idx = attr.indexOf(".");
            CriteriaMethod<T> cm;
            
            cm = getMethod(ob, attr.substring(0,idx));
            if( cm == null ) {
                return null;
            }
            if( idx != (attr.length()-1) ) {
                cm.chain = attr.substring(idx+1);
            }
            return cm;
        }
        if( attr.length() == 1 ) {
            attr = attr.toUpperCase();
        }
        else {
            attr = attr.substring(0, 1).toUpperCase() + attr.substring(1);
        }
        mname = "get" + attr;
        try {
            m = ob.getClass().getMethod(mname, lparams);
            if( m != null ) {
                return new CriteriaMethod<T>(m, this, locale);
            }
        }
        catch( Exception e ) {
            // no localized version, try generic
            try {
                m = ob.getClass().getMethod(mname, params);
                if( m != null ) {
                    return new CriteriaMethod<T>(m, this);
                }
            }
            catch( Exception ignore ) {
                // move on to the next item
            }
        }
        mname = "is" + attr;
        try {
            m = ob.getClass().getMethod(mname, params);
            if( m != null ) {
                return new CriteriaMethod<T>(m, this);
            }
        }
        catch( Exception ignore ) {
            // ignore all exceptions (just checking)
        }
        // let's get creative
        mname = "has" + attr;
        try {
            m = ob.getClass().getMethod(mname, params);
            if( m != null ) {
                return new CriteriaMethod<T>(m, this);
            }
        }
        catch( Exception ignore ) {
            // ignore all exceptions (just checking)
        }
        if( attr.equals("Title") ) {
            try {
                m = ob.getClass().getMethod("getShortTitle", params);
                if( m != null ) {
                    return new CriteriaMethod<T>(m, this);
                }
            }
            catch( Exception ignore ) {
                // ignore all exceptions
            }
            try {
                m = ob.getClass().getMethod("getLongTitle", params);
                if( m != null ) {
                    return new CriteriaMethod<T>(m, this);
                }
            }
            catch( Exception ignore ) {
                // ignore all exceptions
            }
        }
        else if( attr.equals("ShortTitle") || attr.equals("LongTitle") ) {
            try {
                m = ob.getClass().getMethod("getTitle", params);
                if( m != null ) {
                    return new CriteriaMethod<T>(m, this);
                }
            }
            catch( Exception ignore ) {
                // ignore all exceptions
            }            
        }
        return null;
    }
    
    public Object getValue(Object ob) {
        if( ob == null ) {
            return null;
        }
        else if( ob instanceof DelegatedComparable ) {
            DelegatedComparable delegate = (DelegatedComparable)ob;
            
            return delegate.getDelegate(locale);
        }
        else if( ob instanceof Locale ) {
            return ((Locale)ob).getDisplayName(locale);
        }
        return ob;
    }
    
    public boolean isDescending() {
        return descending;
    }

    public boolean represents(String crit, Locale loc, boolean desc) {
    		if( crit == null && criteria.length > 0 ) {
    			return false;
    		}
    		else if( crit != null ) {
    			String[] fields = crit.split(",");
    			
    			if( fields.length != criteria.length ) {
    				return false;
    			}
    			for(int i=0; i<fields.length; i++) {
    				if( !fields[i].equals(criteria[i]) ) {
    					return false;
    				}
    			}
    		}
    		if( !locale.equals(loc) ) {
    			return false;
    		}
    		if( desc != descending ) {
    			return false;
    		}
    		return true;
    }
    
    public void setCriteria(String crit) {
    		if( crit == null ) {
    			criteria = new String[0];
    		}
    		else {
    			criteria = crit.split(",");
    		}
    }

    public void setDescending(boolean desc) {
    		descending = desc;
    }
    
    public void setLocale(Locale loc) {
    		if( loc == null ) {
    			loc = Locale.getDefault();
    		}
    		locale = loc;
    }
    
    @SuppressWarnings("unchecked")
    public int sort(Object a1, Object a2) {
        int x;
        
        if( a1 == null ) {
            if( a2 == null ) {
                x = 0;
            }
            else {
                x = -1;
            }
        }
        else if( a2 == null ) {
            x = 1;
        }
        else {
            if( (a1 instanceof String) && (a2 instanceof String) ) {
                Collator collator = Collator.getInstance(locale);

                collator.setStrength(Collator.SECONDARY);
                x = collator.compare((String)a1, (String)a2);
            }
            else if( (a1 instanceof Comparable<?>) && (a2 instanceof Comparable<?>) ) {
                x = ((Comparable)a1).compareTo(a2);
            }
            else if( a1.equals(a2) ) {
                x = 0;
            }
            else {
                Collator collator = Collator.getInstance(locale);

                collator.setStrength(Collator.SECONDARY);
                x = collator.compare(a1.toString(), a2.toString());
            }
        }
        if( descending ) {
            x = -x;
        }
        return x;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer();
        
        str.append("SORTER: ");
        for(int i=0; i<criteria.length; i++) {
            str.append(criteria[i]);
            str.append(",");
        }
        str.append("/");
        str.append(descending ? "descending" : "ascending");
        return str.toString();
    }
}
