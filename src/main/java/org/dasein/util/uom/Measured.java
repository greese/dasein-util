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

package org.dasein.util.uom;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class Measured<B extends UnitOfMeasure,U extends B> {
    @SuppressWarnings("unchecked")
    static public <T extends Measured<?,?>> T valueOf(@Nonnull Class<T> type, @Nonnull String str) {
        StringBuilder numeric = new StringBuilder();
        StringBuilder unit = new StringBuilder();
        boolean parsingUnits = false;
        
        for( int i=0; i<str.length(); i++ ) {
            char c = str.charAt(i);
            
            if( !parsingUnits ) {
                if( Character.isDigit(c) || c == '.' || c == ',' ) {
                    numeric.append(c);
                }
                else if( Character.isLetter(c) ) {
                    parsingUnits = true;
                    unit.append(c);
                }
            }
            else {
                unit.append(c);
            }
        }
        double quantity = Double.parseDouble(numeric.toString());
        String uomName = unit.toString().trim().toLowerCase();

        try {
            Method m = type.getDeclaredMethod("valueOf", Number.class, String.class);
        
            return (T)m.invoke(null, quantity, uomName);
        }
        catch( Exception e ) {
            throw new IllegalArgumentException(e);
        }
    }
    
    private Number   quantity;
    private U        unitOfMeasure;
    
    public Measured() { }
    
    public Measured(@Nonnull Number value, @Nonnull U uom) {
        this.quantity = value;
        this.unitOfMeasure = uom;
    }
    
    @SuppressWarnings("unchecked")
    public Measured<B,U> add(@Nonnull Measured<B,?> amount) {
        try {
            @SuppressWarnings("rawtypes") Constructor<? extends Measured> c = getClass().getConstructor(Number.class, unitOfMeasure.getRootUnitOfMeasure());

            return c.newInstance(doubleValue() + amount.convertTo(getUnitOfMeasure()).doubleValue(), getUnitOfMeasure());
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException("Can't happen: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public @Nonnull <T extends B> Measured<B,T> convertTo(@Nonnull T targetUom) {
        double converted = doubleValue() * (getUnitOfMeasure().getBaseUnitConversion()/targetUom.getBaseUnitConversion()); 
        
        try {
            @SuppressWarnings("rawtypes") Constructor<? extends Measured> c = getClass().getConstructor(Number.class, targetUom.getRootUnitOfMeasure());

            return c.newInstance(converted, targetUom);
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException("Can't happen: " + e.getMessage());
        }        
    }
    
    public double doubleValue() {
        return quantity.doubleValue();
    }

    @SuppressWarnings("unused")
    public float floatValue() {
        return quantity.floatValue();
    }
    
    public @Nonnull Number getQuantity() {
        return quantity;
    }
    
    public @Nonnull U getUnitOfMeasure() {
        return unitOfMeasure;
    }

    @SuppressWarnings("unused")
    public int intValue() {
        return quantity.intValue();
    }
    
    public long longValue() {
        return quantity.longValue();
    }

    @SuppressWarnings("unused")
    public short shortValue() {
        return quantity.shortValue();
    }

    @SuppressWarnings({"unused", "unchecked"})
    public @Nonnull Measured<B,U> subtract(Measured<B,?> amount) {
        try {
            @SuppressWarnings("rawtypes") Constructor<? extends Measured> c = getClass().getConstructor(Number.class, unitOfMeasure.getRootUnitOfMeasure());

            return c.newInstance(doubleValue() - amount.convertTo(getUnitOfMeasure()).doubleValue(), getUnitOfMeasure());
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException("Can't happen: " + e.getMessage());
        }
    }
    
    @Override
    public @Nonnull String toString() {
        return getUnitOfMeasure().format(getQuantity());
    }

    @SuppressWarnings("unused")
    public void setQuantity(@Nonnull Number quantity) {
        this.quantity = quantity;
    }

    @SuppressWarnings("unused")
    public void setUnitOfMeasure(@Nonnull U unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }
}
