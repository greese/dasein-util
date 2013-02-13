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

public abstract class CursorPopulator<T> {
    private ForwardCursor<T> cursor;

    public CursorPopulator(org.dasein.util.uom.time.TimePeriod<?> timeout) {
        cursor = new ForwardCursor<T>(timeout);
    }

    public CursorPopulator(String name, org.dasein.util.uom.time.TimePeriod<?> timeout) {
        cursor = new ForwardCursor<T>(name, timeout);
    }
    
    public ForwardCursor<T> getCursor() {
        return cursor;
    }

    private class CursorPopulatorTask implements Runnable {
        @Override
        public void run() {
            try {
                populate(cursor);
                cursor.complete();
            }
            catch( Throwable t ) {
                cursor.error(t);
            }
        }
    }
    
    public void populate() {
        DaseinUtilTasks.submit(new CursorPopulatorTask());
    }
    
    public abstract void populate(ForwardCursor<T> cursor);

    public void setSize(int size) {
        cursor.setSize(size);
    }
}
