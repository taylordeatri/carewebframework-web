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
package org.carewebframework.web.client;

import java.util.Collection;
import java.util.Map;

import org.carewebframework.web.ancillary.IElementIdentifier;
import org.carewebframework.web.component.BaseComponent;
import org.springframework.web.socket.WebSocketSession;

/**
 * Synchronizes state changes from the server to the client.
 */
public class Synchronizer {
    
    private final ClientInvocationQueue queue;
    
    private final WebSocketSession session;
    
    private boolean queueing;
    
    public Synchronizer(WebSocketSession session) {
        this.session = session;
        queue = new ClientInvocationQueue();
    }
    
    public void startQueueing() {
        queueing = true;
    }
    
    public void stopQueueing() {
        queueing = false;
        sendToClient(queue.flush());
    }
    
    public void clear() {
        queue.clear();
    }
    
    public Synchronizer createWidget(BaseComponent parent, Map<String, Object> props, Map<String, Object> state) {
        return sendToClient(new ClientInvocation(null, "cwf.widget.create", parent, props, state));
    }
    
    public Synchronizer processQueue(ClientInvocationQueue queue) {
        return sendToClient(queue.flush());
    }
    
    public Synchronizer invokeClient(IElementIdentifier component, String function, Object... args) {
        ClientInvocation invocation = new ClientInvocation(component, function, args);
        return sendToClient(invocation);
    }
    
    public Synchronizer sendToClient(ClientInvocation invocation) {
        if (queueing) {
            queue.queue(invocation);
        } else {
            WebSocketHandler.send(session, invocation);
        }
        
        return this;
    }
    
    public Synchronizer sendToClient(Collection<ClientInvocation> invocations) {
        if (queueing) {
            queue.queue(invocations);
        } else {
            WebSocketHandler.send(session, invocations);
        }
        
        return this;
    }
}
