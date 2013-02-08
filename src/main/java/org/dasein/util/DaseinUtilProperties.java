/**
 * Copyright (C) 1998-2013 enStratus Networks Inc
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

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

public class DaseinUtilProperties {
    static public final String PROPERTIES = "/dasein-util.properties";

    static public final String TASKS_ENABLED = "dasein.util.tasks.enabled";

    static private Logger logger = Logger.getLogger(DaseinUtilProperties.class);
    static private final Properties properties = new Properties();

    static private final boolean taskSystemEnabled;
    static {
        loadPropertiesFile();
        loadSystemProperties(); // anything passed -D will override the file
        taskSystemEnabled = isTasksEnabled();
    }

    /**
     * @return true if task system is configured
     */
    static public boolean isTaskSystemEnabled() {
        return taskSystemEnabled;
    }

    // ----------------
    // Private methods:
    // ----------------

    static private boolean isTasksEnabled() {
        if (!properties.containsKey(TASKS_ENABLED)) {
            return false;
        }
        return "true".equalsIgnoreCase(properties.getProperty(TASKS_ENABLED));
    }

    static private void mergeProperty(String key, Properties props) {
        if (key == null || props == null) {
            return;
        }
        String value = props.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            properties.setProperty(key, value);
        }
    }

    static private void loadSystemProperties() {
        try {
            Properties systemProps = System.getProperties();
            mergeProperty(TASKS_ENABLED, systemProps);
        } catch (Throwable t) {
            logger.error("Problem merging dasein-util properties: " + t.getMessage());
        }
    }

    static private void loadPropertiesFile() {
        try {
            InputStream is = DaseinUtilProperties.class.getResourceAsStream(PROPERTIES);
            try {
                if( is != null ) {
                    properties.load(is);
                }
            } finally {
                if( is != null ) {
                    is.close();
                }
            }
        } catch (Throwable t) {
            logger.error("Problem loading dasein-util properties: " + t.getMessage());
        }
    }
}
