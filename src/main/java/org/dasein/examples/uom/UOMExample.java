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

package org.dasein.examples.uom;

import org.dasein.util.uom.time.Day;
import org.dasein.util.uom.time.TimePeriod;
import org.dasein.util.uom.time.Week;

import javax.annotation.Nonnull;

/**
 * Demonstrates the use of the Dasein unit of measures libraries.
 * @author George Reese (george.reese@imaginary.com)
 * @since 2012.02
 * @version 2012.02
 */
public class UOMExample {
    static public void main(@Nonnull String ... args) {
        // construct a value of 2 weeks
        TimePeriod<Week> weeks = new TimePeriod<Week>(2, TimePeriod.WEEK);

        System.out.println("Initial: " + weeks);

        // add a week
        weeks = (TimePeriod<Week>)weeks.add(new TimePeriod<Week>(1, TimePeriod.WEEK));
        System.out.println("Add a week: " + weeks);

        // subject 3 days
        weeks = (TimePeriod<Week>)weeks.subtract(new TimePeriod<Day>(3, TimePeriod.DAY));
        System.out.println("Subtract 3 days: " + weeks);

        // how about in days?
        TimePeriod<Day> days = (TimePeriod<Day>)weeks.convertTo(TimePeriod.DAY);
        System.out.println("In days: " + days);
    }
}
