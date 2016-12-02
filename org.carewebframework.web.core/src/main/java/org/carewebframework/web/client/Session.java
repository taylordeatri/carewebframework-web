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

import javax.servlet.ServletContext;

import org.carewebframework.web.component.Page;
import org.carewebframework.web.page.PageRegistry;
import org.springframework.web.socket.WebSocketSession;

public class Session {
    
    private final ServletContext servletContext;
    
    private final WebSocketSession socket;
    
    private final Synchronizer synchronizer;
    
    private final long creationTime;
    
    private long lastActivity;
    
    private Page page;
    
    protected Session(ServletContext servletContext, WebSocketSession socket) {
        this.servletContext = servletContext;
        this.socket = socket;
        this.synchronizer = new Synchronizer(socket);
        creationTime = System.currentTimeMillis();
        lastActivity = creationTime;
    }
    
    protected void destroy() {
        if (page != null) {
            try {
                synchronizer.startQueueing();
                page.destroy();
            } finally {
                page = null;
            }
        }
    }
    
    public String getId() {
        return socket.getId();
    }
    
    public long getCreationTime() {
        return creationTime;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public void updateLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    public ServletContext getServletContext() {
        return servletContext;
    }
    
    public WebSocketSession getSocket() {
        return socket;
    }
    
    public Synchronizer getSynchronizer() {
        return synchronizer;
    }
    
    public Page getPage() {
        return page;
    }
    
    protected boolean init(String pageId) {
        if (page != null) {
            if (!page.getId().equals(pageId)) {
                throw new RuntimeException("Page ids do not match.");
            }
            
            return false;
        } else {
            page = PageRegistry.getPage(pageId);
            
            if (page == null) {
                throw new RuntimeException("Unknown page id.");
            }
            
            return true;
        }
    }
}
