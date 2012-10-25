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

package org.dasein.media.io;

import java.io.IOException;
import java.io.InputStream;

public class MultiInputStream extends InputStream {
    private boolean     broken    = false;
    private boolean     extended  = false;
    private int         cacheSize = 0;
    private int[]       data      = null;
    private int         index     = 0;
    private InputStream input     = null;

    public MultiInputStream(InputStream is) {
        super();
        input = is;
    }
    
    public void close() throws IOException {
        input.close();
        data = null;
        extended = false;
        broken = false;
    }

    public int read() throws IOException {
        int b;

        if( index >= cacheSize ) {
            if( broken && !extended ) {
                throw new FullBufferException();
            }
            b = input.read();
            if( b == -1 ) {
                return -1;
            }
            cacheSize++;
            if( data == null ) {
                data = new int[1024];
            }
            if( index >= data.length && !broken ) {
                int[] tmp = new int[data.length*2];

                for(int i=0; i<data.length; i++) {
                    tmp[i] = data[i];
                }
                data = tmp;
                if( data.length >= 4000000 ) {
                    broken = true;
                    extended = true;
                }
            }
            if( !broken ) {
                data[index++] = b;
            }
        }
        else {
            b = data[index++];
        }
        return b;
    }

    public void reset() {
        index = 0;
        extended = false;
    }
}
