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

/* $Id: CacheLoader.java,v 1.1 2005/08/15 16:19:34 george Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

/**
 * <p>
 *   Loads a cached item from its persistent data store. The loader is used by a
 *   {@link ConcurrentCache} to load a value when not present in the cache.
 * </p>
 * <p>
 *   Last modified: $Date: 2005/08/15 16:19:34 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 * @param <T> the type of objects being loaded by the cache loader
 */
public interface CacheLoader<T> {
    /**
     * Loads a cached item for the concurrent cache. If the item does not exist,
     * this method should return <code>null</code>.
     * @return a valid item for the cache or <code>null</code>
     */
    public abstract T load(Object ... args);
}
