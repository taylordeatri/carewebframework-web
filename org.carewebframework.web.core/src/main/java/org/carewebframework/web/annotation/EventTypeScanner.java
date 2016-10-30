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

import java.util.Map;

import org.carewebframework.common.RegistryMap;
import org.carewebframework.common.RegistryMap.DuplicateAction;
import org.carewebframework.web.event.Event;

/**
 * Utility class for scanning method annotations and building component definitions from them.
 */
public class EventTypeScanner extends AbstractClassScanner<Event, EventType> {
    
    private static final EventTypeScanner instance = new EventTypeScanner();
    
    private final Map<String, Class<? extends Event>> typeToClass = new RegistryMap<>(DuplicateAction.ERROR);
    
    public static EventTypeScanner getInstance() {
        return instance;
    }
    
    private EventTypeScanner() {
        super(Event.class, EventType.class);
    }
    
    /**
     * Creates and registers a component definition for a class by scanning the class and its
     * superclasses for method annotations.
     * 
     * @param eventClass Class to scan.
     */
    @Override
    protected void scanClass(Class<Event> eventClass) {
        typeToClass.put(getEventType(eventClass), eventClass);
    }
    
    public Class<? extends Event> getEventClass(String eventType) {
        Class<? extends Event> eventClass = typeToClass.get(eventType);
        return eventClass == null ? Event.class : eventClass;
    }
    
    public String getEventType(Class<? extends Event> eventClass) {
        EventType eventType = eventClass.getAnnotation(EventType.class);
        return eventType == null ? null : eventType.value();
    }
}
