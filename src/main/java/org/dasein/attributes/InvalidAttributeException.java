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

/* $Id: InvalidAttributeException.java,v 1.2 2006/01/24 17:59:14 greese Exp $ */
/* Copyright 2003-2006 Valtira Corporation, All Rights Reserved */
package org.dasein.attributes;

/**
 * <p>
 *   Represents some kind of error instantiating an attribute or its associated data type.
 * </p>
 * <p>
 *   This class was originally developed for the 
 *   <a href="http://simplicis.valtira.com" title="Simplicis web site">Simplicis Content
 *   Management System</a> in 2003 and moved into an Open Source library in 2006.
 * </p>
 * <p>
 *   Last modified: $Date: 2006/01/24 17:59:14 $
 * </p>
 * @version $Revision: 1.2 $
 * @author George Reese
 */
public class InvalidAttributeException extends RuntimeException {
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 3257001038589277241L;

    /**
     * Constructs a new exception with no error message. Avoid using this meaningless constructor.
     */
	public InvalidAttributeException() {
        super();
    }

    /**
     * Constructs a new exception that describes why it could not manage the attribute in question.
     * @param msg the error message
     */
    public InvalidAttributeException(String msg) {
        super(msg);
    }
}
