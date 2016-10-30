/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2016 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.web.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LogUtil {
    
    public static enum LogLevel {
        UNKNOWN, DEBUG, ERROR, FATAL, INFO, TRACE, WARN
    }
    
    public static enum LogTarget {
        NONE, CLIENT, SERVER, BOTH;
    }
    
    private static Map<LogLevel, LogTarget> settings = new HashMap<>();
    
    private static String clientSettings;
    
    public static void initSettings(Map<String, String> settings) {
        for (Entry<String, String> entry : settings.entrySet()) {
            setTarget(toLevel(entry.getKey()), toTarget(entry.getValue()));
        }
    }
    
    public static LogTarget getTarget(LogLevel level) {
        LogTarget target = settings.get(level);
        return target == null ? LogTarget.NONE : target;
    }
    
    public static LogTarget setTarget(LogLevel level, LogTarget target) {
        synchronized (settings) {
            clientSettings = null;
            
            if (target == null || target == LogTarget.NONE) {
                return settings.remove(level);
            }
            
            return settings.put(level, target);
        }
    }
    
    public static String getSettingsForClient() {
        return clientSettings == null ? initSettingsForClient() : clientSettings;
    }
    
    private static String initSettingsForClient() {
        synchronized (settings) {
            StringBuilder sb = new StringBuilder("{");
            String delim = "";
            
            for (Entry<LogLevel, LogTarget> entry : settings.entrySet()) {
                if (entry.getValue() != null && entry.getValue() != LogTarget.NONE) {
                    sb.append(delim).append(entry.getKey().name().toLowerCase()).append(":")
                            .append(entry.getValue().ordinal());
                    delim = ",";
                }
            }
            
            clientSettings = sb.append("}").toString();
        }
        
        return clientSettings;
    }
    
    public static LogLevel toLevel(String level) {
        try {
            return LogLevel.valueOf(level.toUpperCase());
        } catch (Exception e) {
            return LogLevel.UNKNOWN;
        }
    }
    
    public static LogTarget toTarget(String target) {
        try {
            return LogTarget.valueOf(target.toUpperCase());
        } catch (Exception e) {
            return LogTarget.NONE;
        }
    }
    
    private LogUtil() {
    }
}
