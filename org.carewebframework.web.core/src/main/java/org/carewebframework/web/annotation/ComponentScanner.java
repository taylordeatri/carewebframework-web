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
package org.carewebframework.web.annotation;

import java.lang.reflect.Method;

import org.carewebframework.web.annotation.Component.AttributeProcessor;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.BaseComponent;

/**
 * Utility class for scanning method annotations and building component definitions from them.
 */
public class ComponentScanner extends AbstractClassScanner<BaseComponent, Component> {
    
    private static final ComponentScanner instance = new ComponentScanner();
    
    public static ComponentScanner getInstance() {
        return instance;
    }
    
    private ComponentScanner() {
        super(BaseComponent.class, Component.class);
    }
    
    /**
     * Creates and registers a component definition for a class by scanning the class and its
     * superclasses for method annotations.
     * 
     * @param clazz Class to scan.
     */
    @Override
    protected void scanClass(Class<BaseComponent> clazz) {
        ComponentDefinition def = new ComponentDefinition(clazz);
        Class<?> nextClass = clazz;
        
        while (nextClass != Object.class) {
            scanMethods(def, nextClass);
            nextClass = nextClass.getSuperclass();
        }
        
        ComponentRegistry.getInstance().register(def);
    }
    
    /**
     * Scans a class for method annotations, adding them to the component definition as they are
     * found.
     * 
     * @param def Component definition for the class.
     * @param clazz The class to be scanned.
     */
    private void scanMethods(ComponentDefinition def, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            PropertySetter setter = method.getAnnotation(PropertySetter.class);
            
            if (setter != null) {
                def._addSetter(setter, method);
            }
            
            PropertyGetter getter = method.getAnnotation(PropertyGetter.class);
            
            if (getter != null) {
                def._addGetter(getter, method);
            }
            
            AttributeProcessor processor = method.getAnnotation(AttributeProcessor.class);
            
            if (processor != null) {
                def._addProcessor(processor, method);
            }
        }
    }
    
}