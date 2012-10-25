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

package org.dasein.attributes.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dasein.util.CalendarWrapper;

public class PerformanceFilter implements Filter {
    static private class Incoming {
        public long   requestId;
        public String requestUri;
        public long   start;
        
        public boolean equals(Object ob) {
            Incoming inc;
            
            if( ob == null ) {
                return false;
            }
            if( ob == this ) {
                return true;
            }
            inc = (Incoming)ob;
            return requestId == inc.requestId;
        }
        
        public int hashCode() {
            return (new Long(requestId)).hashCode();
        }
    }
    
    static private boolean             checking      = false;
    static private boolean             cleaning      = false;
    static private Logger              logger        = Logger.getLogger(PerformanceFilter.class);
    static private long                nextRequestId = 0L;
    static private Map<Long,Incoming>  requests      = new HashMap<Long,Incoming>();
    static private ArrayList<Incoming> toAdd         = new ArrayList<Incoming>();
    static private ArrayList<Long>     toDelete      = new ArrayList<Long>();
    
    static {
        Thread t = new Thread() {
            public void run() {
                while( true ) { 
                    try { Thread.sleep(30000L); }
                    catch( InterruptedException e ) { }
                    check();
                }
            }
        };
        
        t.setName("Performance Monitor");
        t.setDaemon(true);
        t.start();
    }
    
    static private void check() {
        synchronized( requests ) {
            checking = true;
        }
        try {
            long now = System.currentTimeMillis();
            long diff = CalendarWrapper.SECOND * 30L;
            ArrayList<Long> tmp;
            
            for( Map.Entry<Long,Incoming> entry : requests.entrySet() ) {
                Incoming inc = entry.getValue();
                long elapsed;
                
                elapsed = now - inc.start;
                if( elapsed >= diff ) {
                    synchronized( toDelete ) {
                        if( toDelete.contains(inc.requestId) ) {
                            continue;
                        }
                    }
                    logger.warn("DELAY " + inc.requestId + " (" + elapsed + " millis): " + inc.requestUri);
                }
            }
            synchronized( requests ) {
                cleaning = true;
            }
            synchronized( toAdd ) {
                for( Incoming inc : toAdd ) {
                    // no need to sync here, since this thread only adds
                    // and syncing might create a deadlock
                    requests.put(inc.requestId, inc);
                }
                toAdd.clear();
            }
            tmp = new ArrayList<Long>();
            synchronized( toDelete ) {
                tmp.addAll(toDelete);
                toDelete.clear();
            }
            synchronized( requests ) {
                for( long id : tmp ) {
                    requests.remove(id);
                }
            }
        }
        finally {
            synchronized( requests ) {
                checking = false;
                cleaning = false;
            }
        }
    }
    
    static private void end(long id) {
        logger.debug("enter - end(long)");
        if( logger.isDebugEnabled() ) {
            logger.debug("long=" + id);
        }
        try {
            long now = System.currentTimeMillis();
            Incoming inc = null;
            boolean td = false;
            boolean check;
            
            synchronized( requests ) {
                inc = requests.get(id);
                if( inc != null && (!checking || cleaning) ) {
                    check = false;
                    requests.remove(id);                    
                }
                else if( inc != null ) {
                    td = true;
                }
                check = (inc == null && checking);
            }
            if( td  ) {
                toDelete.add(id);                
            }
            if( check ) {
                synchronized( toAdd ) {
                    for( Incoming ni : toAdd ) {
                        if( ni.requestId == id ) {
                            inc = ni;
                            toAdd.remove(ni);
                            break;
                        }
                    }
                }
                // a tiny probability that the first check could return null, but the second one not
                if( inc == null ) {
                    synchronized( requests ) {
                        inc = requests.get(id);
                        if( inc != null && (!checking || cleaning) ) {
                            requests.remove(id);
                        }
                        else {
                            td = true;
                        }
                    }
                    if( td ) {
                        synchronized( toDelete ) {
                            toDelete.add(id);
                        }
                    }
                }
            }
            if( inc == null ) {
                logger.error("Unable to find request: " + id);
                return;
            }
            logger.info("END " + id + " (" + inc.requestUri + "): " + (now - inc.start) + " millis");
        }
        finally {
            logger.debug("exit = end(long)");
            if( logger.isDebugEnabled() ) {
                logger.debug("long=" + id);
            }
        }
    }
    
    static private long start(HttpServletRequest req) {
        logger.debug("enter - start(HttpServletRequest)");
        if( logger.isDebugEnabled() ) {
            logger.debug("req=" + req);
        }
        try {
            long start = System.currentTimeMillis();
            Incoming inc = new Incoming();
            boolean add = false;
            
            inc.start = start;
            inc.requestUri = req.getRequestURI() + " [" + req.getQueryString() + "]";
            synchronized( requests ) {
                inc.requestId = nextRequestId++;
            }
            synchronized( requests ) {
                if( !checking || cleaning ) {
                    requests.put(inc.requestId, inc);
                }
                else {
                    add = true;
                }
            }
            if( add ) {
                synchronized( toAdd ) {
                    toAdd.add(inc);
                }
            }
            logger.info("START " + inc.requestId + ": " + inc.requestUri);
            return inc.requestId;
        }
        finally {
            logger.debug("exit - start(HttpServletRequest)");
            if( logger.isDebugEnabled() ) {
                logger.debug("req=" + req);
            }
        }
    }
    
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        long id = start((HttpServletRequest)req);
        
        try {
            chain.doFilter(req, resp);
        }
        finally {
            end(id);
        }
    }

    public void destroy() {
        // NO-op
    }

    public void init(FilterConfig cfg) throws ServletException {
        // NO-op
    }
}
