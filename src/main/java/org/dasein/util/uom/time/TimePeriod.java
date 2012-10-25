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

import javax.annotation.Nonnull;

public class TimePeriod<T extends TimePeriodUnit> extends Measured<TimePeriodUnit,T> {
    static public final Microsecond MICROSECOND = new Microsecond();
    static public final Millisecond MILLISECOND = new Millisecond();
    static public final Second      SECOND      = new Second();
    static public final Minute      MINUTE      = new Minute();
    static public final Hour        HOUR        = new Hour();
    static public final Day         DAY         = new Day();
    static public final Week        WEEK        = new Week();
    
    static public void main(String ... args) {
        TimePeriod<? extends TimePeriodUnit> memory = TimePeriod.valueOf(args[0]);
        TimePeriodUnit uom = TimePeriodUnit.valueOf(args[1]);

        System.out.println(memory.convertTo(uom));  
    }
        
    @SuppressWarnings("unchecked")
    static public @Nonnull TimePeriod<? extends TimePeriodUnit> valueOf(@Nonnull String str) {
        return Measured.valueOf(TimePeriod.class, str);
    }
    
    static public @Nonnull TimePeriod<? extends TimePeriodUnit> valueOf(@Nonnull Number quantity, @Nonnull String uomName) {
        return new TimePeriod<TimePeriodUnit>(quantity, TimePeriodUnit.valueOf(uomName));
    }

    @SuppressWarnings("unused")
    public TimePeriod() { }
    
    public TimePeriod(@Nonnull Number quantity, @Nonnull T uom) {
        super(quantity, uom);
    }
}
