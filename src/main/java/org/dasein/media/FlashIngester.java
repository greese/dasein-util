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

package org.dasein.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.dasein.media.io.FlashInputStream;

public class FlashIngester implements Ingester {
    static public final MimeType MIME_TYPE 
        = new MimeType("application", "x-shockwave-flash");
    
    private FlashInputStream   input     = null;
    private Map<String,Object> metaData  = null;
    private long               size      = -1L;
    
    public void clear() {
        input = null;
        metaData = new HashMap<String,Object>();
    }

    public Map<String,Object> getMetaData() {
        return metaData;
    }
    
    public MimeType getMimeType() {
        return MIME_TYPE;
    }

    public long getSize() {
        return size;
    }
    
    @SuppressWarnings("unused") 
    public void ingest() throws IOException, InvalidMediaTypeException {
        boolean compressed;
        int sz, max_x, max_y, min_x, min_y;
        int c;

        c = (int)input.readUnsignedInt(1);        
        if( c == (int)'C' ) {
            compressed = true;
        }
        else if( c == (int)'F' ) {
            compressed = false;
        }
        else {
            throw new InvalidMediaTypeException("Invalid SWF signature.");
        }
            System.out.println("Compressed: " + compressed);
        c = (int)input.readUnsignedInt(1);
        if( c != (int)'W' ) {
            throw new InvalidMediaTypeException("Invalid SWF signature.");
        }
        c = (int)input.readUnsignedInt(1);
        if( c != (int)'S' ) {
            throw new InvalidMediaTypeException("Invalid SWF signature.");
        }
        metaData.put(EXTENSION, "swf");
        metaData.put("version", String.valueOf(input.readUnsignedInt(1)));
        size = input.readUnsignedInt(4);
        if( compressed ) {
            input.setCompressed();
        }
        sz = (int)input.readUnsignedBits(5);
        min_x = (int)input.readSignedBits(sz);
        max_x = (int)input.readSignedBits(sz);
        min_y = (int)input.readSignedBits(sz); // ignore the fact that this is unused
        max_y = (int)input.readSignedBits(sz); // ignore the fact that this is unused
        metaData.put(HEIGHT, new Integer(max_y/20));
        metaData.put(WIDTH, new Integer(max_x/20));
        metaData.put("frameRate", new Integer(((int)input.readUnsignedInt(2))>>8));
        metaData.put("frameCount", new Integer((int)input.readUnsignedInt(2)));
    }
    
    public void setInput(InputStream in) {
        clear();
        input = new FlashInputStream(in);
    }

    static public void main(String[] args) {
        try {
            if( args.length != 1 ) {
                System.out.println("Usage: java org.dasein.media.FlashIngester FILE");
            }
            else {
                FlashIngester ingester = new FlashIngester();

                ingester.setInput(new java.io.FileInputStream(args[0]));
                System.out.println("Ingesting " + args[0] + "...");
                ingester.ingest();
                System.out.println("Read " + ingester.getSize() + " bytes.");
                System.out.println("Meta-data: " + ingester.getMetaData());
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
    }
}
