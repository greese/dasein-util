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

package org.dasein.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimePeriod {
    private long endTimestamp;
    private long startTimestamp;
    
    public TimePeriod() { }
    
    public TimePeriod(long startTimestamp, int durationInHours, int durationInMinutes) {
        this.startTimestamp = startTimestamp;
    }
    
    public boolean contains(long when) {
        return ((startTimestamp <= when) && (endTimestamp > when));
    }
    
    public long getEndTimestamp() {
        return endTimestamp;
    }
    
    public long getStartTimestamp() {
        return startTimestamp;
    }
    
    private transient volatile String periodString = null;
    
    public String toString() {
        String ps;
        
        synchronized( this ) {
            ps = periodString;
        }
        if( ps == null ) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            StringBuilder str = new StringBuilder();
            
            str.append(fmt.format(new Date(startTimestamp)));
            str.append("/");
            str.append(fmt.format(new Date(endTimestamp)));
            ps = str.toString();
            synchronized( this ) {
                periodString = ps;
            }
        }
        return ps;
    }
}
