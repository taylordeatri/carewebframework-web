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

import org.carewebframework.web.component.BaseComponent;

/**
 * Represents a registry of component names for a namespace.
 */
public class NameRegistry {
    
    private final Map<String, BaseComponent> names = new HashMap<>();
    
    public void register(BaseComponent component, boolean register) {
        Map<String, BaseComponent> map = register ? new HashMap<>() : null;
        register(component, map);
        
        if (map != null) {
            names.putAll(map);
        }
    }
    
    private void register(BaseComponent component, Map<String, BaseComponent> map) {
        String name = component.getName();
        
        if (name != null) {
            if (map == null) {
                names.remove(name);
            } else {
                BaseComponent current = names.get(name);
                current = current == null ? map.get(name) : current;
                
                if (current != null && current != component) {
                    throw new ComponentException(current, "Name already exists in current namespace: " + name);
                }
                
                map.put(name, component);
            }
        }
        
        if (!(component instanceof INamespace)) {
            for (BaseComponent child : component.getChildren()) {
                register(child, map);
            }
        }
        
    }
    
    public BaseComponent get(String name) {
        return names.get(name);
    }
    
}
