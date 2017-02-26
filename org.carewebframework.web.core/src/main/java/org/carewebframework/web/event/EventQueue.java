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

import java.util.LinkedList;

import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.websocket.Session;

/**
 * A page's queue for posted events. Queued (posted) events are delivered at the end of an execution
 * cycle. If an event is queued outside of the target page's execution context, it will be delivered
 * at the end of the next execution cycle for that page. To ensure timely delivery in such an
 * instance, a ping request is sent to the client in order to trigger an execution cycle for the
 * target page.
 */
public class EventQueue {
    
    private final LinkedList<Event> queue = new LinkedList<>();
    
    private final Page page;
    
    public EventQueue(Page page) {
        this.page = page;
    }
    
    public synchronized void queue(Event event) {
        if (event.getPage() != page) {
            throw new RuntimeException("Event does not belong to this queue's page");
        }
        
        queue.add(event);
        
        if (queue.size() == 1 && ExecutionContext.getPage() != page) {
            Session session = page.getSession();
            
            if (session != null) {
                session.ping("flush");
            }
        }
    }
    
    public synchronized void processAll() {
        while (!queue.isEmpty()) {
            Event event = queue.removeFirst();
            EventUtil.send(event, event.getTarget() == null ? page : event.getTarget());
        }
    }
    
    public synchronized void clearAll() {
        queue.clear();
    }
}
