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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public enum MediaType  {
    APPLET(1), AUDIO(2), FLASH(3), IMAGE(4), VIDEO(5), DOCUMENT(6);

    static private final String PROPERTIES = "/dasein-media.properties";
    
    static private final Map ingesters;
    
    static {
        Map<String,Ingester> map = new HashMap<String,Ingester>();
        
        try {
            Properties props = new Properties();

            try {
                Enumeration e;
                InputStream is;

                is = MediaType.class.getResourceAsStream(PROPERTIES);
                if( is != null ) {
                    props.load(is);
                }
                e = props.propertyNames();
                while( e.hasMoreElements() ) {
                    String nom = (String)e.nextElement();
                    String cname = props.getProperty(nom);

                    nom = nom.toLowerCase();
                    try {
                        Ingester ing;

                        ing = (Ingester)Class.forName(cname).newInstance();
                        if( map.containsKey(nom) ) {
                            Ingester orig = (Ingester)map.get(nom);

                            if( orig instanceof MultiIngester ) {
                                ((MultiIngester)orig).add(ing);
                            }
                            else {
                                ing = new MultiIngester(orig, ing);
                            }
                        }
                        map.put(nom, ing);
                    }
                    catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }
            }
            catch( IOException e ) {
                e.printStackTrace();
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        ingesters = map;
    }
    
    /**
     * @deprecated This is now an enum
     */
    static public MediaType getInstance(int c) {
        for( MediaType t : values() ) {
            if( c == t.code ) {
                return t;
            }
        }
        return null;
    }
    
    private int code = 4;
    
    private MediaType(int c) {
        code = c;
    }

    /**
     * @deprecated This is now a traditional enum.
     */
    public int getCode() {
        return code;
    }

    public Ingester getIngester() {
        String nom = getName();
        
        if( ingesters.containsKey(nom) ) {
            return (Ingester)ingesters.get(nom);
        }
        return null;
    }
    
    public String getName() {
        return toString().toLowerCase();
    }
}
