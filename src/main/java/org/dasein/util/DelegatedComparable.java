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

/* $Id: DelegatedComparable.java,v 1.1 2005/08/29 01:39:09 george Exp $ */
/* Copyright (c) 2005 Valtira Corporation, All Rights Reserved */
package org.dasein.util;

import java.util.Locale;

/**
 * <p>
 *   Used by the {@link GenericSorter} to delegate sorting on this object to some
 *   other object.
 * </p>
 * <p>
 *   Last modified: $Date: 2005/08/29 01:39:09 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public interface DelegatedComparable {
    public abstract Object getDelegate(Locale loc);
}
