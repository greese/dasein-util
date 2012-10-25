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

import java.io.Serializable;
import java.util.Currency;
import java.util.Locale;
import java.text.NumberFormat;

/**
 * //TODO javadoc
 *
 * @author Morgan Catlin <morgan.catlin@valtira.com>.
 * @version 1.0
 */
public class Money implements Comparable<Money>, Serializable {
    private static final long serialVersionUID = -6201030428991925852L;

    static public Money valueOf(String str) {
        Currency curr = null;
        double val;
        int idx;

        idx = str.indexOf(" ");
        if( idx == -1 ) {
            if( str.length() > 3 ) {
                boolean ic = true;

                for(int i=0; i<3; i++) {
                    if( !Character.isLetter(str.charAt(i)) ) {
                        ic = false;
                    }
                }
                if( ic ) {
                    idx = 2;
                    curr = Currency.getInstance(str.substring(0,3));
                }
            }
        }
        else {
            curr = Currency.getInstance(str.substring(0,idx));
        }
        if( curr == null ) {
            Locale loc = Locale.getDefault();

            curr = NumberFormat.getCurrencyInstance(loc).getCurrency();
        }
        val = Double.parseDouble(str.substring(idx+1));
        return new Money(curr, val);
    }

    private Currency currency;
    private double   value;

    public Money(Locale loc, long val) {
        this(NumberFormat.getCurrencyInstance(loc).getCurrency(), val);
    }

    public Money(Currency curr, long val) {
        this(curr, (double)val);
    }

    public Money(Locale loc, Long val) {
        this(loc, val.doubleValue());
    }

    public Money(Currency curr, Long val) {
        this(curr, val.doubleValue());
    }

    public Money(Locale loc, double val) {
        this(NumberFormat.getCurrencyInstance(loc).getCurrency(), val);
    }

    public Money(Currency curr, double val) {
        super();
        currency = curr;
        value = val;
    }

    public Money(Locale loc, Double val) {
        this(loc, val.doubleValue());
    }

    public Money(Currency curr, Double val) {
        this(curr, val.doubleValue());
    }

    public Money(Locale loc, long whole, int part) {
        this(NumberFormat.getCurrencyInstance(loc).getCurrency(), whole, part);
    }

    public Money(Currency curr, long whole, int part) {
        this(curr, ((double)whole) + ((double)((part>99)?99:part))/100);
    }

    public Money(Locale loc, Number whole, Number part) {
        this(loc, whole.longValue(), part.intValue());
    }

    public Money(Currency curr, Number whole, Number part) {
        this(curr, whole.longValue(), part.intValue());
    }

    public Money add(Money amt) {
        if( !getCurrency().equals(amt.getCurrency()) ) {
            throw new CurrencyMismatchException(getCurrency(), amt.getCurrency());
        }
        else {
            return new Money(currency, value + amt.getValue());
        }
    }

    public int compareTo(Money other) {
        int x;
        
        if( other == null ) {
            return 1;
        }
        if( other == this ) {
            return 0;
        }
        if( !currency.equals(other.currency) ) {
            String code = currency.getCurrencyCode();

            return code.compareTo(other.currency.getCurrencyCode());
        }
        x = (new Long(getWholeValue())).compareTo(other.getWholeValue());
        if( x == 0 ) {
            x = (new Integer(getFractionValue())).compareTo(new Integer(other.getFractionValue()));
        }
        return x;
    }

    public boolean equals(Object ob) {
        Money other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (Money)ob;
        if( !getCurrency().equals(other.getCurrency()) ) {
            return false;
        }
        if( getWholeValue() != other.getWholeValue() ) {
            return false;
        }
        return (getFractionValue() == other.getFractionValue());
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getString(Locale loc) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(loc);

        fmt.setCurrency(currency);
        return fmt.format(value);
    }

    public double getValue() {
        return value;
    }

    public long getWholeValue() {
        return Math.round(getValue() - getValue()%1);
    }
    
    public int getFractionValue() {
        int frac = getCurrency().getDefaultFractionDigits();

        if( frac < 1 ) {
            return 0;
        }
        return Math.round((float)(Math.pow(10, frac-1) * getValue()%1));
    }
    
    private transient int hashCode = -1;
    
    public int hashCode() {
        if( hashCode == -1 ) {  
            hashCode = (currency.getCurrencyCode() + value).hashCode();
        }
        return hashCode;
    }

    public Money subtract(Money amt) {
        if( !getCurrency().equals(amt.getCurrency()) ) {
            throw new CurrencyMismatchException(getCurrency(), amt.getCurrency());
        }
        else {
            return new Money(currency, value - amt.getValue());
        }
    }

    public String toString() {
        return (currency.getCurrencyCode() + " " + value);
    }
}
