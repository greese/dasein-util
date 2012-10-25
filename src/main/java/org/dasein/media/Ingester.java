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

/* $Id: Ingester.java,v 1.1 2006/05/21 20:34:38 greese Exp $ */
/* Copyright (c) 2004 Valtira Corporation, All Rights Reserved */
package org.dasein.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * <p>
 * Interface for media-independent ingesting of media assets. Different kinds of media
 * will have different implementations of this interface to discern their meta-data.
 * </p>
 * <p>
 * Last modified $Date: 2006/05/21 20:34:38 $
 * </p>
 * @version $Revision: 1.1 $
 * @author George Reese
 */
public interface Ingester {
    static public final String EXTENSION = "extension";
    static public final String HEIGHT    = "height";
    static public final String WIDTH     = "width";

    /**
     * Clears the ingester to handle a new media stream.
     */
    void clear();

    /**
     * Provides the meta-data associated with the last ingested media asset.
     * @return the asset's meta-data
     */
    Map<String,Object> getMetaData();

    /**
     * @return the mime type for the most recently read media asset
     */
    MimeType getMimeType();

    /**
     * If the ingest process requires multiple passes in order to determine file
     * size, this method should return -1 to allow the requesting application
     * to determine size on its own.
     * @return the size in bytes of the asset or -1
     */
    long getSize();
    
    /**
     * Performs an ingest of the specified stream. This method will initiate reading
     * from the stream and parsing it for the ingest. The method returns once the
     * media stream has been fully read.
     * @throws java.io.IOException an error occurred reading the input stream
     * @throws org.dasein.media.InvalidMediaTypeException the media in the input stream
     * is not one supported by this ingester
     */
    void ingest() throws IOException, InvalidMediaTypeException;

    /**
     * Sets an input stream to be ingested. This method implicitly calls the 
     * {@link #clear()} method to clear out any information from a previous
     * ingest.
     * @param is the input stream to be ingested
     */
    void setInput(InputStream is);
}
