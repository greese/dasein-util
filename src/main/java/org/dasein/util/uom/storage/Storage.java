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

public class Storage<T extends StorageUnit> extends Measured<StorageUnit,T> {
    static public final Bit       BIT      = new Bit();
    static public final Byte      BYTE     = new Byte();
    static public final Gigabyte  GIGABYTE = new Gigabyte();
    static public final Kilobyte  KILOBYTE = new Kilobyte();
    static public final Megabyte  MEGABYTE = new Megabyte();
    static public final Terabyte  TERABYTE = new Terabyte();
    
    static public void main(String ... args) {
        Storage<? extends StorageUnit> memory = Storage.valueOf(args[0]);
        StorageUnit uom = StorageUnit.valueOf(args[1]);
        
        System.out.println(memory.convertTo(uom));  
    }
        
    @SuppressWarnings("unchecked")
    static public Storage<? extends StorageUnit> valueOf(String str) {
        return Measured.valueOf(Storage.class, str);
    }
    
    static public Storage<? extends StorageUnit> valueOf(Number quantity, String uomName) {
        StorageUnit uom = StorageUnit.valueOf(uomName);

        return new Storage<StorageUnit>(quantity, uom);
    }
    
    public Storage() { }
    
    public Storage(Number quantity, T uom) {
        super(quantity, uom);
    }
}
