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
package org.carewebframework.web.websocket;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.web.client.ClientInvocation;
import org.carewebframework.web.client.ClientRequest;
import org.carewebframework.web.client.Synchronizer;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.page.PageRegistry;
import org.springframework.web.socket.WebSocketSession;

/**
 * Container for core resources for a single client session (i.e., web socket connection).
 */
public class Session {

    private static final Log log = LogFactory.getLog(Session.class);

    protected enum SessionEvent {
        INIT, DESTROY, REQUEST
    }

    private final ServletContext servletContext;

    private final WebSocketSession socket;

    private final Synchronizer synchronizer;

    private Set<ISessionListener> sessionListeners;
    
    private final long creationTime;

    private long lastActivity;

    private Page page;

    /**
     * Create a session, with references to its servlet context and web socket.
     *
     * @param servletContext The servlet context.
     * @param socket The web socket.
     */
    protected Session(ServletContext servletContext, WebSocketSession socket) {
        this.servletContext = servletContext;
        this.socket = socket;
        this.synchronizer = new Synchronizer(socket);
        creationTime = System.currentTimeMillis();
        lastActivity = creationTime;
    }

    /**
     * Destroy the session. This destroys the associated page.
     */
    protected void destroy() {
        if (page != null) {
            try {
                synchronizer.startQueueing();
                page.destroy();
            } finally {
                page = null;
            }
        }

        notifySessionListeners(SessionEvent.DESTROY, null);
    }

    /**
     * Returns the session's id, which is the same as the underlying web socket id.
     *
     * @return The session's id.
     */
    public String getId() {
        return socket.getId();
    }

    /**
     * Returns the session's time of creation.
     *
     * @return The session's time of creation.
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Returns the time of last activity for the session.
     *
     * @return The time of last activity.
     */
    public long getLastActivity() {
        return lastActivity;
    }

    /**
     * Updates the session's last activity.
     */
    public void updateLastActivity() {
        this.lastActivity = System.currentTimeMillis();
    }

    /**
     * Returns the servlet context associated with the session.
     *
     * @return The servlet context.
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Returns the web socket associated with the session.
     *
     * @return The web socket.
     */
    public WebSocketSession getSocket() {
        return socket;
    }

    /**
     * Returns the synchronizer associated with the session.
     *
     * @return The synchronizer.
     */
    public Synchronizer getSynchronizer() {
        return synchronizer;
    }

    /**
     * Returns the page associated with the session.
     *
     * @return The page.
     */
    public Page getPage() {
        return page;
    }

    /**
     * Register a session listener.
     *
     * @param listener Session listener to register.
     * @return True if the operation was successful.
     */
    public boolean registerSessionListener(ISessionListener listener) {
        if (sessionListeners == null) {
            sessionListeners = new LinkedHashSet<>();
        }
        
        return sessionListeners.add(listener);
    }
    
    /**
     * Unregister a session listener.
     *
     * @param listener Session listener to unregister.
     * @return True if the operation was successful.
     */
    public boolean unregisterSessionListener(ISessionListener listener) {
        return sessionListeners != null && sessionListeners.remove(listener);
    }
    
    /**
     * Notify all session listeners of an event.
     *
     * @param event The type of session event.
     * @param request The client request, if this is a request event.
     */
    protected void notifySessionListeners(SessionEvent event, ClientRequest request) {
        if (sessionListeners != null) {
            for (ISessionListener sessionListener : sessionListeners) {
                try {
                    switch (event) {
                        case INIT:
                            sessionListener.onInitialize();
                            break;

                        case DESTROY:
                            sessionListener.onDestroy();
                            break;

                        case REQUEST:
                            sessionListener.onClientRequest(request);
                            break;
                    }
                } catch (Exception e) {
                    log.error("A session listener threw an exception", e);
                }
            }
        }
    }

    /**
     * Send a ping to the client.
     *
     * @param data Data to send with the ping.
     */
    public void ping(String data) {
        WebSocketHandler.send(socket, new ClientInvocation((String) null, "cwf.ws.ping", data));
    }

    /**
     * Initialize the session. If already initialized, this only validates that the page id matches
     * that of the associated page. Otherwise, it associates the session with the page specified by
     * the page id.
     *
     * @param pageId The page id.
     * @return True if initialization took place.
     */
    protected boolean _init(String pageId) {
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

            notifySessionListeners(SessionEvent.INIT, null);
            return true;
        }
    }
}
