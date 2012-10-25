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

public class RawValue extends Measured<DimensionlessUnit,Dimensionless> {
    static public final Dimensionless DIMENSIONLESS = new Dimensionless();
    
    static public @Nonnull RawValue valueOf(@Nonnull  String str) {
        return Measured.valueOf(RawValue.class, str);
    }
    
    static public @Nonnull RawValue valueOf(@Nonnull Number quantity, @Nullable String uomName) {
        if( uomName == null || uomName.equals("") ) {
            return new RawValue(quantity);
        }
        throw new IllegalArgumentException("Dimensionless values should not have a unit of measure: " + uomName);
    }

    @SuppressWarnings("unused")
    protected RawValue() { }

    public RawValue(@Nonnull Number quantity) {
        super(quantity, RawValue.DIMENSIONLESS);
    }

    @SuppressWarnings("unused")
    public RawValue(@Nonnull Number quantity, @Nonnull Dimensionless dim) {
        super(quantity, dim);
    }
}
