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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

public class FlashInputStream extends InputStream {
    private class Buffer {
        public int count     = 0;
        public int remainder = 0;
    }
    
    private InputStream input     = null;
    private Buffer      buffer    = null;
    
    public FlashInputStream(InputStream is) {
        super();
        input = is;
    }

    public int read() throws IOException {
        if( buffer == null ) {
            return input.read();
        }
        else if( buffer.count == 8 ) {
            int val = buffer.remainder;
            
            buffer = null;
            return val;
        }
        else {
            int b = input.read();
            int val;

            if( b == -1 ) {
                val = buffer.remainder;
                buffer = null;
            }
            else {
                val = (buffer.remainder << (8-buffer.count));
                val |= (b >> buffer.count);
                buffer.remainder = b;
            }
            return val;
        }
    }

    public long readBits(int count) throws IOException {
        long val = 0L;
        
        if( count < 1 ) {
            return 0;
        }
        if( buffer == null ) {
            buffer = new Buffer();
            buffer.count = 0;
        }
        if( buffer.count < 1 ) {
            buffer.count = 8;
            buffer.remainder = input.read();
            if( buffer.remainder == -1 ) {
                throw new EOFException("Stream is smaller than requested.");
            }
        }
        while( count > 0 ) {
            if( count == buffer.count ) {
                val |= buffer.remainder;
                buffer = null;
                count = 0;
            }
            else if( count > buffer.count ) {
                val |= (buffer.remainder << (count-8));
                count -= buffer.count;
                buffer.remainder = input.read();
                if( buffer.remainder == -1 ) {
                    throw new EOFException("Stream is smaller than requested.");
                }
                buffer.count = 8;
            }
            else {
                int mask = (0xff >> count);

                val |= (buffer.remainder >> (buffer.count - count));
                buffer.remainder = ((buffer.remainder & mask) << count);
                buffer.count -= count;
                count = 0;
            }
        }
        return val;
    }

    public byte[] readBytes(int count) throws IOException {
        byte[] data = new byte[count];
        int sz = 0;

        buffer = null;
        if( count < 1 ) {
            return data;
        }
        while( sz < count ) {
            int num = input.read(data, sz, count-sz);

            if( num == -1 ) {
                throw new EOFException("Stream is smaller than requested.");
            }
            sz += num;
        }
        return data;
    }
    
    public long readSignedBits(int count) throws IOException {
        long val = readUnsignedBits(count);

        if( (val & (1L << (count-1))) != 0 ) {
            val |= (-1L << count);
        }
        return val;
    }

    public long readSignedInt(int count) throws IOException {
        long val = 0L;
        byte[] sign;
        
        buffer = null;
        if( count < 1 ) {
            return 0L;
        }
        for(int i=(count-1); i>0; i--) {
            int b = input.read();
            
            if( b < 0 ) {
                throw new EOFException("Stream is smaller than requested.");
            }
            val += b * (long)Math.pow((double)256, (double)((count-1)-i));
        }
        sign = readBytes(1);
        val += sign[0] * (long)Math.pow((double)256, (double)(count-1));
        return val;
    }
    
    public long readUnsignedBits(int count) throws IOException {
        return readBits(count);
    }

    public long readUnsignedInt(int count) throws IOException {
        long val;
        
        buffer = null;
        if( count < 1 ) {
            return 0L;
        }
        val = input.read();
        if( val < 0L ) {
            throw new EOFException("Stream is smaller than requested.");
        }
        if( count == 1 ) {
            return val;
        }
        for(int i=1; i<count; i++) {
            long b = input.read();

            if( b < 0L ) {
                throw new EOFException("Stream is smaller than requested.");
            }
            val += (b << (i*8));
        }
        return val;
    }

    public void setCompressed() {
        input = new InflaterInputStream(input);
    }
}
