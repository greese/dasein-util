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

import java.util.Map;

/**
 * <p>
 * Convenience class for things that should "look like" a {@link Map} for the
 * purposes of JSTL, yet are not truly maps.
 * </p>
 * <p>
 *   Last modified: $Date: 2005/08/29 01:39:09 $
 * </p>
 * @version $Revision: 1.4 $
 * @author george
 * @param <K> the type for keys to the map
 * @param <V> the type of values in the map
 */
public abstract class PseudoMap<K,V> implements Map<K,V> {
    /**
     * Does nothing.
     */
    public void clear() {
        /* ignore */
    }

    /**
     * Does nothing.
     */
    public void putAll(Map<? extends K,? extends V> map) {
        /* ignore */
    }

    /**
     * @param key ignored
     * @return never returns
     * @throws UnsupportedOperationException always thrown
     */
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param key ignored
     * @param val ignored
     * @return never returns
     * @throws UnsupportedOperationException always thrown
     */
    public V put(K key, V val) {
        throw new UnsupportedOperationException();
    }
}
