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

import org.dasein.util.CalendarWrapper;
import org.dasein.util.uom.time.Hour;
import org.dasein.util.uom.time.Millisecond;
import org.dasein.util.uom.time.Minute;
import org.dasein.util.uom.time.TimePeriod;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Verifies the functioning of the UOM of code.
 * @author George Reese
 * @since 2012.02
 * @version 2012.02
 */
public class UOMTestCase {
    @Rule
    public TestName testName = new TestName();

    public UOMTestCase() {}

    public String getName() {
        return testName.getMethodName();
    }
    
    @Test
    public void testConvertMinutesToMilliseconds() {
        TimePeriod<Minute> tenMinutes = new TimePeriod<Minute>(10, TimePeriod.MINUTE);
        TimePeriod<Millisecond> millis = (TimePeriod<Millisecond>)tenMinutes.convertTo(TimePeriod.MILLISECOND);

        assertEquals("Values are not equal", CalendarWrapper.MINUTE*10, millis.longValue());
        assertTrue("Result not in minutes", millis.getUnitOfMeasure().equals(TimePeriod.MILLISECOND));
    }

    @Test
    public void testConvertTypes() {
        TimePeriod<Minute> minutes = new TimePeriod<Minute>(10.5, TimePeriod.MINUTE);
        TimePeriod<Millisecond> millis = (TimePeriod<Millisecond>)minutes.convertTo(TimePeriod.MILLISECOND);

        assertEquals("Values are not equal", (CalendarWrapper.MINUTE*10) + (CalendarWrapper.SECOND*30), millis.longValue());
        assertTrue("Result not in minutes", millis.getUnitOfMeasure().equals(TimePeriod.MILLISECOND));
    }
    
    @Test 
    public void testConvertMinutesToMillisecondsAndBack() {
        TimePeriod<Minute> tenMinutes = new TimePeriod<Minute>(10, TimePeriod.MINUTE);
        TimePeriod<Millisecond> millis = (TimePeriod<Millisecond>)tenMinutes.convertTo(TimePeriod.MILLISECOND);

        tenMinutes = (TimePeriod<Minute>)millis.convertTo(TimePeriod.MINUTE);
        assertEquals("Values are not equal", 10L, tenMinutes.longValue());
        assertTrue("Result not in minutes", tenMinutes.getUnitOfMeasure().equals(TimePeriod.MINUTE));
    }

    @Test
    public void testConvertTypesBack() {
        TimePeriod<Minute> minutes = new TimePeriod<Minute>(10.5, TimePeriod.MINUTE);
        TimePeriod<Millisecond> millis = (TimePeriod<Millisecond>)minutes.convertTo(TimePeriod.MILLISECOND);

        minutes = (TimePeriod<Minute>)millis.convertTo(TimePeriod.MINUTE);
        assertEquals("Values are not equal", 10.5, minutes.doubleValue());
        assertTrue("Result not in minutes", minutes.getUnitOfMeasure().equals(TimePeriod.MINUTE));
    }
    
    @Test
    public void testAddSame() {
        TimePeriod<Minute> tenMinutes = new TimePeriod<Minute>(10, TimePeriod.MINUTE);
        TimePeriod<Minute> fiveMinutes = new TimePeriod<Minute>(5, TimePeriod.MINUTE);
        TimePeriod<Minute> fifteenMinutes = (TimePeriod<Minute>)tenMinutes.add(fiveMinutes);

        assertEquals("Values are not equal", 15, fifteenMinutes.intValue());
        assertTrue("Result not in minutes", fifteenMinutes.getUnitOfMeasure().equals(TimePeriod.MINUTE));
    }
    
    @Test
    public void testAddDifferent() {
        TimePeriod<Minute> minutes = new TimePeriod<Minute>(10, TimePeriod.MINUTE);
        TimePeriod<Hour> hour = new TimePeriod<Hour>(1, TimePeriod.HOUR);
        TimePeriod<Minute> result = (TimePeriod<Minute>)minutes.add(hour);

        assertEquals("Values are not equal", 70, result.intValue());
        assertTrue("Result not in minutes", result.getUnitOfMeasure().equals(TimePeriod.MINUTE));
    }
}
