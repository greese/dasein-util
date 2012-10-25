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

/* $Id: CacheManagementException.java,v 1.2 2006/01/29 05:50:02 greese Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

/**
 * <p>
 *   Triggered when requests are made of the cache that don't match the cache
 *   management contract.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/01/29 05:50:02 $
 * </p>
 * @version $Revision: 1.2 $
 * @author George Reese
 */
public class CacheManagementException extends RuntimeException {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3256438123063030069L;

    /**
     * Constructs a new exception with the specified error message.
     * @param msg an error message indicating what went wrong
     */
    public CacheManagementException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs a new exception reflecting the underlying cause.
     * @param cause the cause of this exception being thrown
     */
    public CacheManagementException(Throwable cause) {
        super(cause);
    }
}
