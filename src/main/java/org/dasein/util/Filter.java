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

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Enables rapid development of fast collection filters that potentially map to different
 * collection types.
 * @author George Reese (george.reese@enstratus.com
 * @param <S> the type of objects in the source
 * @param <T> the type of objects in the filtered results
 */
public abstract class Filter<S,T> {
    public Collection<T> filter(Iterable<S> source) {
        final Iterable<S> items = source;
        
        PopulatorThread<T> populator = new PopulatorThread<T>(new JiteratorPopulator<T>() {
            public void populate(@Nonnull Jiterator<T> iterator) {
                for( S item : items ) {
                    T target = test(item);
                    
                    if( target != null ) {
                        iterator.push(target);
                    }
                }
            }
        });
        populator.populate();
        return populator.getResult();
    }
    
    public Collection<T> filter(S[] source) {
        final S[] items = source;
        
        PopulatorThread<T> populator = new PopulatorThread<T>(new JiteratorPopulator<T>() {
            public void populate(@Nonnull Jiterator<T> iterator) {
                if( items != null ) {
                    for( S item : items ) {
                        T target = test(item);
                        
                        if( target != null ) {
                            iterator.push(target);
                        }
                    }
                }
            }
        });
        populator.populate();
        return populator.getResult();
    }
    
    public abstract T test(S item);
}
