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

import java.io.Serializable;

public class MimeType implements Serializable {
    static public MimeType valueOf(String str) {
        if( str == null ) {
            return new MimeType("");
        }
        return new MimeType(str);
    }
    
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -5980159494976494827L;
    
    private String type    = null;
    private String subtype = null;
    
    public MimeType(String tstring) {
        super();
        {
            String[] parts = tstring.split("/");

            if( parts.length == 1 ) {
                type = parts[0];
            }
            else {
                type = parts[0];
                subtype = parts[1];
            }
        }
    }

    public MimeType(String t, String st) {
        super();
        type = t;
        subtype = st;
    }

    public String getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }
    
    public String toString() {
        return (type + "/" + subtype);
    }
}
