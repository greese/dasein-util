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

public class Week extends TimePeriodUnit {
    static public TimePeriod<Week> valueOf(short weeks) {
        return new TimePeriod<Week>(weeks, TimePeriod.WEEK);
    }

    static public TimePeriod<Week> valueOf(int weeks) {
        return new TimePeriod<Week>(weeks, TimePeriod.WEEK);
    }

    static public TimePeriod<Week> valueOf(long weeks) {
        return new TimePeriod<Week>(weeks, TimePeriod.WEEK);
    }

    static public TimePeriod<Week> valueOf(double weeks) {
        return new TimePeriod<Week>(weeks, TimePeriod.WEEK);
    }

    static public TimePeriod<Week> valueOf(float weeks) {
        return new TimePeriod<Week>(weeks, TimePeriod.WEEK);
    }

    static public TimePeriod<Week> valueOf(Number weeks) {
        return new TimePeriod<Week>(weeks, TimePeriod.WEEK);
    }

    public Week() { }
    
    public double getBaseUnitConversion() {
        return 168.0;
    }
    
    @Nonnull
    @Override
    public String format(@Nonnull Number quantity) {
        MessageFormat fmt = new MessageFormat("{0}");

        fmt.setFormatByArgumentIndex(0, new ChoiceFormat(new double[] {0,1,2}, new String[] {"0 weeks","1 week","{0,number} weeks"}));
        return fmt.format(new Object[] { quantity });
    }
}
