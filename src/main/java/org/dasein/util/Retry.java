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

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

public class Retry<T> {
    static private final Logger logger = Logger.getLogger(Retry.class);
    
    static public class RetryException extends RuntimeException {
        private static final long serialVersionUID = -7329075046711516092L;

        public RetryException(String msg) {
            super(msg);
        }
    }

    public T retry(int retries, Callable<T> operation) throws Exception {
        return retry(retries, 2, operation);
    }
    
    public T retry(int retries, int baseDelayInSeconds, Callable<T> operation) throws Exception {
        int maxRetries = retries;
        
        if( retries < 1 ) {
            retries = 1;
        }
        while( retries > 0 ) {
            retries--;
            if( retries > 0 ) {
                try {
                    return operation.call();
                }
                catch( Exception e ) {
                    logger.warn(e);
                    try { Thread.sleep((maxRetries-retries) * baseDelayInSeconds * 1000L); }
                    catch( InterruptedException ignore ) { }
                }
            }
            else {
                return operation.call();
            }
        }
        throw new RetryException("Unreachable situation.");
    }
}
