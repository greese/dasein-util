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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dasein.media.io.MultiInputStream;

public class MultiIngester implements Ingester {
    private Collection<Ingester>  ingesters = null;
    private InputStream           input     = null;
    private Ingester              primary = null;

    public MultiIngester(Ingester first, Ingester second) {
        super();
        ingesters = new ArrayList<Ingester>();
        ingesters.add(first);
        ingesters.add(second);
    }

    public void add(Ingester ing) {
        ingesters.add(ing);
    }
    
    public void clear() {
        Iterator it = ingesters.iterator();

        while( it.hasNext() ) {
            Ingester ing = (Ingester)it.next();

            ing.clear();
        }
        primary = null;
        input = null;
    }

    public Map<String,Object> getMetaData() {
        if( primary == null ) {
            return new HashMap<String,Object>();
        }
        return primary.getMetaData();
    }

    public MimeType getMimeType() {
        if( primary != null ) {
            return primary.getMimeType();
        }
        return null;
    }

    public long getSize() {
        if( primary == null ) {
            return -1L;
        }
        return primary.getSize();
    }

    public void ingest() throws InvalidMediaTypeException, IOException {
        Iterator it;
        
        clear();
        it = ingesters.iterator();
        while( it.hasNext() ) {
            Ingester ing = (Ingester)it.next();

            try {
                ing.ingest();
                input.close();
                primary = ing;
                return;
            }
            catch( InvalidMediaTypeException e ) {
            }
            try {
                input.reset();
            }
            catch( IOException e ) {
                throw new InvalidMediaTypeException("Unable to reset stream.");
            }
        }
        throw new InvalidMediaTypeException("No matches on media type.");
    }

    public void setInput(InputStream is) {
        Iterator it = ingesters.iterator();
        
        input = new MultiInputStream(is);
        while( it.hasNext() ) {
            Ingester ing = (Ingester)it.next();

            ing.setInput(input);
        }
    }
}
