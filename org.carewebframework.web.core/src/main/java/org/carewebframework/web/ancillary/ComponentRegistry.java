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

import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.RegistryMap.DuplicateAction;
import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.component.BaseComponent;

/**
 * Registry of component definitions indexed by their tag name and implementing class.
 */
public class ComponentRegistry extends AbstractRegistry<String, ComponentDefinition> {
    
    private static final ComponentRegistry instance = new ComponentRegistry();
    
    private final Map<Class<? extends BaseComponent>, ComponentDefinition> classToDefinition = new HashMap<>();
    
    public static ComponentRegistry getInstance() {
        return instance;
    }
    
    private ComponentRegistry() {
        super(DuplicateAction.ERROR);
    }
    
    /**
     * Adds a component definition to the registry.
     * 
     * @param item Item to add.
     */
    @Override
    public void register(ComponentDefinition item) {
        super.register(item);
        classToDefinition.put(item.getComponentClass(), item);
    }
    
    @Override
    protected String getKey(ComponentDefinition item) {
        return item.getTag();
    }
    
    @Override
    public ComponentDefinition unregisterByKey(String key) {
        return classToDefinition.remove(super.unregisterByKey(key));
    }
    
    public ComponentDefinition get(Class<? extends BaseComponent> componentClass) {
        ComponentDefinition def = null;
        Class<?> clazz = componentClass;
        
        do {
            def = classToDefinition.get(clazz);
        } while (def == null && BaseComponent.class.isAssignableFrom((clazz = clazz.getSuperclass())));
        
        return def;
    }
    
}
