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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.reflect.FieldUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;

/**
 * Wires {@literal @EventHandler}-annotated methods.
 */
public class EventHandlerScanner {
    
    /**
     * Event listener implementation that invokes an {@literal @EventHandler}-annotated method on a
     * target object.
     */
    private static class EventListener implements IEventListener {
        
        private final Object target;
        
        private final Method method;
        
        EventListener(Object target, Method method) {
            this.target = target;
            this.method = method;
        }
        
        /**
         * Pass an event to the handler method on the target.
         */
        @Override
        public void onEvent(Event event) {
            try {
                if (method.getParameterTypes().length == 0) {
                    method.invoke(target);
                } else {
                    method.invoke(target, event);
                }
            } catch (Throwable e) {
                throw MiscUtil.toUnchecked(e);
            }
        }
        
    }
    
    /**
     * Recursively scans the controller's class and superclasses for {@literal @EventHandler}
     * -annotated methods.
     * 
     * @param instance Controller to be wired.
     * @param root The root component used to resolve component names.
     */
    public static void wire(Object instance, BaseComponent root) {
        Class<?> clazz = instance.getClass();
        
        while (clazz != Object.class) {
            wire(instance, root, clazz);
            clazz = clazz.getSuperclass();
        }
    }
    
    /**
     * Scans the specified class for {@literal @EventHandler}-annotated methods.
     * 
     * @param instance Controller to be wired.
     * @param root The root component used to resolve component names.
     * @param clazz The class to be scanned.
     */
    private static void wire(Object instance, BaseComponent root, Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            EventHandler annot = method.getAnnotation(EventHandler.class);
            
            if (annot == null) {
                continue;
            }
            
            OnFailure onFailure = annot.onFailure();
            Class<?>[] params = method.getParameterTypes();
            
            if (params.length > 1 || (params.length == 1 && !Event.class.isAssignableFrom(params[0]))) {
                onFailure
                        .doAction("Method " + method.getName() + " signature does not conform to that of an event handler.");
                return;
            }
            
            Set<String> targets = asSet(annot.target());
            Set<String> types = asSet(annot.value());
            BaseComponent component = null;
            
            if (types.isEmpty()) {
                onFailure.doAction("At least one event type must be specified");
            }
            
            if (targets.isEmpty()) {
                targets.add("self");
            }
            
            for (String target : targets) {
                if ("self".equals(target)) {
                    component = isComponent(clazz) ? (BaseComponent) instance : root;
                } else if (target.startsWith("@")) {
                    Field field = findField(clazz, target.substring(1));
                    
                    if (field != null) {
                        try {
                            field.setAccessible(true);
                            component = (BaseComponent) field.get(instance);
                        } catch (Exception e) {
                            // what to do here
                        }
                    }
                } else {
                    component = root == null ? null : root.findByName(target);
                }
                
                if (component == null) {
                    onFailure.doAction("No suitable event handler target found for \"" + target + "\"");
                } else {
                    for (String type : types) {
                        component.addEventListener(type, new EventListener(instance, method), annot.syncToClient());
                    }
                }
            }
        }
    }
    
    /**
     * Converts an array of string values to a set. This effectively removes duplicate and empty
     * entries.
     * 
     * @param values Array of string values.
     * @return Corresponding set of values.
     */
    private static Set<String> asSet(String[] values) {
        Set<String> set = new HashSet<>();
        
        for (String value : values) {
            if (!value.isEmpty()) {
                set.add(value);
            }
        }
        
        return set;
    }
    
    /**
     * Returns true if the class is a component class.
     * 
     * @param clazz Class to be examined.
     * @return True if the class is a component class.
     */
    private static boolean isComponent(Class<?> clazz) {
        return BaseComponent.class.isAssignableFrom(clazz);
    }
    
    /**
     * Returns the field definition for the named field. Field must be a component type.
     * 
     * @param clazz Class to be searched.
     * @param name Name of the field.
     * @return The corresponding field definition, or null if no matching field was found.
     */
    private static Field findField(Class<?> clazz, String name) {
        try {
            Field result = FieldUtils.getField(clazz, name, true);
            return isComponent(result.getType()) ? result : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private EventHandlerScanner() {
    }
}
