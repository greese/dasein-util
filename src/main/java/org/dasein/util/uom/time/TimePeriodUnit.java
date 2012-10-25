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

package org.dasein.util.uom.time;

import org.dasein.util.uom.Measured;
import org.dasein.util.uom.UnitOfMeasure;
import org.dasein.util.uom.UnknownUnitOfMeasure;

import javax.annotation.Nonnull;

public abstract class TimePeriodUnit extends UnitOfMeasure {
    static public @Nonnull TimePeriodUnit valueOf(@Nonnull String uom) {
        if( uom.equals("s") || uom.equals("second") || uom.equals("seconds") || uom.equals("sec") ) {
            return TimePeriod.SECOND;
        }
        else if( uom.length() < 1 || uom.equals("minute") || uom.equals("minutes") || uom.equals("m") || uom.equals("min") ) {
            return TimePeriod.MINUTE;
        }
        else if( uom.equals("hour") || uom.equals("hours") || uom.equals("h") || uom.equals("hrs") || uom.equals("hr") ) {
            return TimePeriod.HOUR;
        }
        else if( uom.equals("day") || uom.equals("days") || uom.equals("d") ) {
            return TimePeriod.DAY;
        }
        else if( uom.equals("week") || uom.equals("weeks") || uom.equals("w") || uom.equals("wks") || uom.equals("wk") ) {
            return TimePeriod.WEEK;
        }
        else if( uom.equals("ms") || uom.equals("millisecondsecond") || uom.equals("milliseconds") || uom.equals("millis") ) {
            return TimePeriod.MILLISECOND;
        }
        else if( uom.equals("μs") || uom.equals("microecondsecond") || uom.equals("microseconds") || uom.equals("micros") ) {
            return TimePeriod.MICROSECOND;
        }
        throw new UnknownUnitOfMeasure(uom);
    }
    
    @Override
    public @Nonnull Class<TimePeriodUnit> getRootUnitOfMeasure() {
        return TimePeriodUnit.class;
    }

    @Override
    public @Nonnull UnitOfMeasure getBaseUnit() {
        return TimePeriod.HOUR;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @Nonnull <B extends UnitOfMeasure, U extends B> Measured<B, U> newQuantity(@Nonnull Number quantity) {
        return new TimePeriod(quantity, this);
    }
}
