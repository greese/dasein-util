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

public class InvalidMediaTypeException extends Exception {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7003557838595436179L;

    public InvalidMediaTypeException() {
        super();
    }

    public InvalidMediaTypeException(String msg) {
        super(msg);
    }

    public InvalidMediaTypeException(Exception cause) {
        super(cause);
    }
}
