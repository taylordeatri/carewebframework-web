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
package org.carewebframework.web.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class EventListeners {
    
    private final Map<String, Set<IEventListener>> allListeners = new HashMap<>();
    
    public void add(String eventType, IEventListener eventListener) {
        getListeners(eventType, true).add(eventListener);
    }
    
    public void remove(String eventType, IEventListener eventListener) {
        Set<IEventListener> listeners = getListeners(eventType, false);
        
        if (listeners != null) {
            listeners.remove(eventListener);
            
            if (listeners.isEmpty()) {
                allListeners.remove(eventType);
            }
        }
    }
    
    public void removeAll() {
        allListeners.clear();
    }
    
    public void removeAll(String eventType) {
        allListeners.remove(eventType);
    }
    
    public void invoke(Event event) {
        Set<IEventListener> listeners = getListeners(event.getType(), false);
        
        if (listeners != null) {
            for (IEventListener listener : new ArrayList<>(listeners)) {
                if (event.isStopped()) {
                    break;
                }
                
                listener.onEvent(event);
            }
        }
    }
    
    public boolean hasListeners(String eventType) {
        Set<IEventListener> listeners = getListeners(eventType, false);
        return listeners != null && !listeners.isEmpty();
    }
    
    private Set<IEventListener> getListeners(String eventType, boolean forceCreate) {
        Set<IEventListener> listeners = allListeners.get(eventType);
        
        if (listeners == null && forceCreate) {
            allListeners.put(eventType, listeners = new LinkedHashSet<IEventListener>());
        }
        
        return listeners;
    }
    
    public String getClientEvents() {
        StringBuilder sb = new StringBuilder();
        
        for (String eventType : allListeners.keySet()) {
            sb.append(sb.length() == 0 ? "" : " ").append(eventType);
        }
        
        return sb.toString();
    }
}
