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
import javax.annotation.Nullable;

public abstract class UnitOfMeasure {
    public UnitOfMeasure() { } 
    
    public boolean equals(@Nullable Object ob) {
        return (ob != null && ob.getClass().getName().equals(getClass().getName()));
    }
    
    public abstract double getBaseUnitConversion();
    
    public abstract @Nonnull String format(@Nonnull Number quantity);

    @SuppressWarnings("unused")
    public abstract @Nonnull UnitOfMeasure getBaseUnit();
    
    public abstract @Nonnull Class<? extends UnitOfMeasure> getRootUnitOfMeasure();

    @SuppressWarnings("unused")
    public abstract @Nonnull <B extends UnitOfMeasure,U extends B> Measured<B,U> newQuantity(@Nonnull Number quantity);
}
