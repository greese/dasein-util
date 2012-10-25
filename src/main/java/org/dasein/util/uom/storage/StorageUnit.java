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

package org.dasein.util.uom.storage;

import org.dasein.util.uom.Measured;
import org.dasein.util.uom.UnitOfMeasure;
import org.dasein.util.uom.UnknownUnitOfMeasure;

import javax.annotation.Nonnull;

public abstract class StorageUnit extends UnitOfMeasure {
    static public @Nonnull StorageUnit valueOf(@Nonnull String uom) {
        if( uom.length() < 1 || uom.equals("bit") || uom.equals("bits") || uom.equals("b") ) {
            return Storage.BIT;
        }
        else if( uom.equals("byte") || uom.equals("bytes") ) {
            return Storage.BYTE;
        }
        else if( uom.equals("kb") || uom.equals("kilobyte") || uom.equals("kilobytes") || uom.equals("kbyte") || uom.equals("kbytes") ) {
            return Storage.KILOBYTE;
        }
        else if( uom.equals("mb") || uom.equals("megabyte") || uom.equals("megabytes") || uom.equals("mbyte") || uom.equals("mbytes") ) {
            return Storage.MEGABYTE;
        }
        else if( uom.equals("gb") || uom.equals("gigabyte") || uom.equals("gigabytes") || uom.equals("gbyte") || uom.equals("gbytes") ) {
            return Storage.GIGABYTE;
        }
        else if( uom.equals("tb") || uom.equals("terabyte") || uom.equals("terabytes") || uom.equals("tbyte") || uom.equals("tbytes") ) {
            return Storage.TERABYTE;
        }
        throw new UnknownUnitOfMeasure(uom);
    }
    
    @Nonnull
    @Override
    public Class<StorageUnit> getRootUnitOfMeasure() {
        return StorageUnit.class;
    }

    @Nonnull
    @Override
    public UnitOfMeasure getBaseUnit() {
        return Storage.BYTE;
    }

    @Nonnull
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <B extends UnitOfMeasure, U extends B> Measured<B, U> newQuantity(@Nonnull Number quantity) {
        return new Storage(quantity, this);
    }
}
