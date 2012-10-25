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

import javax.annotation.Nonnull;
import java.text.ChoiceFormat;
import java.text.MessageFormat;

public class Hour extends TimePeriodUnit {
    static public TimePeriod<Hour> valueOf(short hours) {
        return new TimePeriod<Hour>(hours, TimePeriod.HOUR);
    }

    static public TimePeriod<Hour> valueOf(int hours) {
        return new TimePeriod<Hour>(hours, TimePeriod.HOUR);
    }

    static public TimePeriod<Hour> valueOf(long hours) {
        return new TimePeriod<Hour>(hours, TimePeriod.HOUR);
    }

    static public TimePeriod<Hour> valueOf(double hours) {
        return new TimePeriod<Hour>(hours, TimePeriod.HOUR);
    }

    static public TimePeriod<Hour> valueOf(float hours) {
        return new TimePeriod<Hour>(hours, TimePeriod.HOUR);
    }

    static public TimePeriod<Hour> valueOf(Number hours) {
        return new TimePeriod<Hour>(hours, TimePeriod.HOUR);
    }
    
    public Hour() { }
    
    public double getBaseUnitConversion() {
        return 1.0;
    }
    
    @Override
    public @Nonnull String format(@Nonnull Number quantity) {
        MessageFormat fmt = new MessageFormat("{0}");

        fmt.setFormatByArgumentIndex(0, new ChoiceFormat(new double[] {0,1,2}, new String[] {"0 hours","1 hour","{0,number} hours"}));
        return fmt.format(new Object[] { quantity });
    }
}
