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

/* $Id: CachedItem.java,v 1.2 2005/08/15 16:19:34 george Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

/**
 * <p>
 *   Represents an object that may expire in one of the Dasein-managed caches. Objects
 *   in these caches ({@link Cache}, {@link ConcurrentCache}, and {@link ConcurrentMultiCache})
 *   do NOT have to implement this interface. Implementing this interface simply helps them
 *   expire from the cache according to their own rules. For example, you might want to
 *   set up persistent objects that are stored in a database to expire from the cache
 *   every 5 minutes so that the environment will pick up any changes made against the
 *   database.
 * </p>
 * <p>
 *   Last modified: $Date: 2005/08/15 16:19:34 $
 * </p>
 * @version $Revision: 1.2 $
 * @author George Reese
 */
public interface CachedItem {
    /**
     * This method tells the containing cache whether this object remains valid for the
     * cache. The implementation determines the rules for what is valid and what is 
     * invalid. This method is NOT for garbage collection purposes. The Dasein caches
     * use soft references and will expire any cached objects from memory in accordance
     * with the rules for normal Java garbage collection.
     * @return true if the object should remain in the cache
     */
    public abstract boolean isValidForCache();
}
