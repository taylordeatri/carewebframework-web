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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.util.StringUtils;

/**
 * Manages CSS classes for a component. There are two semantic extensions to CSS class names:
 * <ol>
 * <li>A "-" character preceding a class name indicates that it is to be removed.</li>
 * <li>A group name may precede a class name, separated by a ":" character. When a class with a
 * group name is added, any previous class associated with that group will be removed. A group name
 * with no following class name simply removes the class currently associated with that group. This
 * facilitates managing classes that are mutually exclusive.</li>
 * </ol>
 */
public class CssClasses {
    
    private final Set<String> classes = new TreeSet<>();
    
    private final Map<String, String> groups = new HashMap<>();
    
    private boolean changed;
    
    public CssClasses parse(String value) {
        classes.clear();
        groups.clear();
        update(false, value);
        return this;
    }
    
    public boolean add(String... values) {
        return update(false, values);
    }
    
    public boolean remove(String... values) {
        return update(true, values);
    }
    
    public boolean toggle(String yesValue, String noValue, boolean condition) {
        return update(condition, noValue) | update(!condition, yesValue);
    }
    
    private boolean update(boolean remove, String... values) {
        for (String value : values) {
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            
            for (String cls : value.split("\\ ")) {
                cls = cls.trim();
                boolean _remove = remove;
                
                if (cls.startsWith("-")) {
                    cls = cls.substring(1);
                    _remove = true;
                }
                
                if (cls.contains(":")) {
                    String[] pcs = cls.split("\\:", 2);
                    cls = pcs[1];
                    String group = pcs[0];
                    String clazz = groups.get(group);
                    changed |= clazz != null && classes.remove(clazz);
                    groups.put(group, _remove ? "" : cls);
                }
                
                if (!cls.isEmpty()) {
                    if (_remove) {
                        changed |= classes.remove(cls);
                    } else {
                        changed |= classes.add(cls);
                    }
                }
            }
        }
        
        return changed;
    }
    
    public boolean hasChanged() {
        return changed;
    }
    
    public boolean isEmpty() {
        return classes.isEmpty();
    }
    
    public String toString(boolean clearChanged) {
        StringBuilder sb = new StringBuilder();
        
        for (String cls : classes) {
            sb.append(sb.length() == 0 ? "" : " ").append(cls);
        }
        
        changed = clearChanged || changed;
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return toString(false);
    }
}
