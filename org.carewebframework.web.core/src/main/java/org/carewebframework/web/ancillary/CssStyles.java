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
package org.carewebframework.web.ancillary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

public class CssStyles {
    
    private final Map<String, String> styles = new LinkedHashMap<>();
    
    public void parse(String value, boolean clear) {
        if (clear) {
            styles.clear();
        }
        
        if (StringUtils.isEmpty(value)) {
            return;
        }
        
        for (String style : value.split("\\;")) {
            String[] pcs = style.split("\\:", 2);
            String name = pcs[0].trim();
            value = pcs.length == 1 ? "" : pcs[1].trim();
            
            if (value.isEmpty()) {
                styles.remove(name);
            } else {
                styles.put(name, value);
            }
        }
    }
    
    public Map<String, String> get() {
        return Collections.unmodifiableMap(styles);
    }
    
    public String get(String style) {
        return styles.get(style);
    }
    
    public String put(String style, String value) {
        return value == null ? remove(style) : styles.put(style, value);
    }
    
    public String remove(String style) {
        return styles.remove(style);
    }
    
    public boolean isEmpty() {
        return styles.isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (Entry<String, String> entry : styles.entrySet()) {
            if (entry.getValue() != null) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                
                sb.append(entry.getKey()).append(':').append(entry.getValue());
            }
        }
        
        return sb.toString();
    }
}
