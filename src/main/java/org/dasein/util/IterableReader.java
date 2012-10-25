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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

public class IterableReader implements Iterable<String> {
    private BufferedReader reader;
    
    public IterableReader(BufferedReader reader) {
        this.reader = reader;
    }
    
    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            public String nextLine = null;
            
            @Override
            public boolean hasNext() {
                if( nextLine == null ) {
                    try {
                        nextLine();
                    }
                    catch( IOException e ) {
                        // TODO: implement me
                    }
                }
                return (nextLine != null);
            }

            @Override
            public String next() {
                if( nextLine == null ) {
                    try {
                        nextLine();
                    }
                    catch( IOException e ) {
                        // TODO: implement me
                    }
                }
                if( nextLine == null ) {
                    // TODO: throw exception
                }
                String str = nextLine;
                
                nextLine = null;
                return str;
            }

            private void nextLine() throws IOException {
                nextLine = reader.readLine();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove lines from an iterable reader.");
            }
        };
    }
}
