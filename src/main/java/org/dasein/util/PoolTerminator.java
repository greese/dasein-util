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

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class PoolTerminator implements ServletContextListener {
    static public final Logger logger = Logger.getLogger(PoolTerminator.class);
    
    static public ArrayList<Callable<Boolean>> terminationHandlers = new ArrayList<Callable<Boolean>>();
    
    static public void addTerminationHandler(Callable<Boolean> handler) {
        synchronized( terminationHandlers ) {
            terminationHandlers.add(handler);
        }
    }
    
    public void contextDestroyed(ServletContextEvent event) {
        logger.debug("enter - contextDestroyed(ServletContextEvent)");
        try {
            synchronized( terminationHandlers ) {
                for( Callable<Boolean> handler : terminationHandlers ) {
                    try {
                        Boolean success = handler.call();
                        
                        if( success == null || !success ) {
                            logger.warn("Failure from " + handler.getClass().getName());
                        }
                    }
                    catch( Exception e ) {
                        logger.warn(handler.getClass().getName() + ": " + e.getMessage());
                    }
                }
            }
        }
        finally {
            logger.debug("exit - contextDestroyed(ServletContextEvent)");            
        }
    }

    public void contextInitialized(ServletContextEvent event) {
        // NO-OP
    }
}
