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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

/**
 * Keeps track of active sessions.
 */
public class Sessions implements BeanPostProcessor {
    
    private static final Log log = LogFactory.getLog(Sessions.class);

    private static final Sessions instance = new Sessions();
    
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    private final Set<ISessionTracker> sessionTrackers = new HashSet<>();
    
    public static Sessions getInstance() {
        return instance;
    }
    
    private Sessions() {
    }

    /**
     * Returns a read-only list of all active sessions.
     *
     * @return List of all active sessions.
     */
    public Collection<Session> getActiveSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }
    
    /**
     * Registers a session tracker.
     *
     * @param tracker A session tracker.
     */
    public void registerSessionTracker(ISessionTracker tracker) {
        sessionTrackers.add(tracker);
    }
    
    /**
     * Unregisters a session tracker.
     *
     * @param tracker A session tracker.
     */
    public void unregisterSessionTracker(ISessionTracker tracker) {
        sessionTrackers.remove(tracker);
    }
    
    /**
     * Notify session trackers of a session-related event.
     *
     * @param session Session triggering the event.
     * @param created If true, it is a create event; if false, a destroy event.
     */
    protected void notifySessionTrackers(Session session, boolean created) {
        if (!sessionTrackers.isEmpty()) {
            for (ISessionTracker tracker : new ArrayList<>(sessionTrackers)) {
                try {
                    if (created) {
                        tracker.onSessionCreate(session);
                    } else {
                        tracker.onSessionDestroy(session);
                    }
                } catch (Exception e) {
                    log.error("A session tracker threw an exception.", e);
                }
            }
        }
    }
    
    /**
     * Looks up a session by its unique id.
     *
     * @param id The session id.
     * @return The associated session, or null if none found.
     */
    public Session getSession(String id) {
        return sessions.get(id);
    }
    
    /**
     * Creates and registers a new session.
     *
     * @param servletContext The servlet context.
     * @param socket The web socket session.
     * @return The newly created session.
     */
    protected Session createSession(ServletContext servletContext, WebSocketSession socket) {
        Session session = new Session(servletContext, socket);
        sessions.put(session.getId(), session);
        
        if (log.isDebugEnabled()) {
            logSessionEvent(session, "established");
        }

        return session;
    }
    
    /**
     * Destroys and unregisters the session associated with the specified web socket.
     *
     * @param socket The web socket session.
     * @param status The close status.
     */
    protected void destroySession(WebSocketSession socket, CloseStatus status) {
        Session session = sessions.remove(socket.getId());
        
        if (session != null) {
            if (log.isDebugEnabled()) {
                logSessionEvent(session, "closed, " + status);
            }
            
            notifySessionTrackers(session, false);
            session.destroy();
        }
    }
    
    /**
     * Logs a session event.
     *
     * @param session The session.
     * @param event The text describing the event.
     */
    private void logSessionEvent(Session session, String event) {
        log.debug("Session #" + session.getId() + " " + event + ".");
    }
    
    /**
     * NOP
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    /**
     * Detects and registers session trackers.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ISessionTracker) {
            registerSessionTracker((ISessionTracker) bean);
        }
        
        return bean;
    }
    
}
