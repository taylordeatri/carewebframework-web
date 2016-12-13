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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.MiscUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Handler for all web socket communications.
 */
public class WebSocketHandler extends TextWebSocketHandler implements BeanPostProcessor, ServletContextAware {
    
    /**
     * Interface for handling a client request. A request handler must be registered for each
     * expected request type.
     */
    public interface IRequestHandler {
        
        /**
         * Handle a client request.
         * 
         * @param request The client request.
         * @throws Exception Unspecified exception.
         */
        void handleRequest(ClientRequest request) throws Exception;
        
        /**
         * @return The type of request handled.
         */
        String getRequestType();
    }
    
    private static final Log log = LogFactory.getLog(WebSocketHandler.class);
    
    private static final String ATTR_BUFFER = "message_buffer";
    
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    
    private static final Set<ISessionTracker> sessionTrackers = new HashSet<>();
    
    private static final Map<String, IRequestHandler> handlers = new HashMap<>();
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private static final ObjectReader reader = mapper.readerFor(Map.class);
    
    private static final ObjectWriter writer = mapper.writerFor(Map.class);
    
    private ServletContext servletContext;
    
    public static Collection<Session> getActiveSessions() {
        return Collections.unmodifiableCollection(sessions.values());
    }
    
    /**
     * Register a request handler.
     * 
     * @param handler The request handler.
     */
    public static void registerRequestHandler(IRequestHandler handler) {
        String type = handler.getRequestType();
        
        if (handlers.containsKey(type)) {
            throw new RuntimeException("Attempt to register a duplicate request handler for request type: " + type);
        }
        
        handlers.put(type, handler);
    }
    
    public static void registerSessionTracker(ISessionTracker tracker) {
        sessionTrackers.add(tracker);
    }
    
    public static void unregisterSessionTracker(ISessionTracker tracker) {
        sessionTrackers.remove(tracker);
    }
    
    /**
     * Sends a json payload to the client via the web socket session.
     * 
     * @param socket The web socket session. If null, the session is derived from the current
     *            execution context.
     * @param json The json payload.
     */
    private static void sendData(WebSocketSession socket, String json) {
        try {
            socket = socket == null ? ExecutionContext.getSession().getSocket() : socket;
            socket.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    /**
     * Sends a client invocation request to the client via the web socket session derived from the
     * current execution context.
     * 
     * @param invocation The client invocation request.
     */
    public static void send(ClientInvocation invocation) {
        send(null, invocation);
    }
    
    /**
     * Sends a client invocation request to the client via the web socket session.
     * 
     * @param socket The web socket session. If null, the session is derived from the current
     *            execution context.
     * @param invocation The client invocation request.
     */
    public static void send(WebSocketSession socket, ClientInvocation invocation) {
        try {
            String json = writer.writeValueAsString(invocation.toMap());
            sendData(socket, json);
        } catch (Exception e) {
            log.error(e);
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Sends multiple client invocation requests to the client via the web socket session derived
     * from the current execution context.
     * 
     * @param invocations The client invocation requests.
     */
    public static void send(Collection<ClientInvocation> invocations) {
        send(null, invocations);
    }
    
    /**
     * Sends multiple client invocation requests to the client via the web socket session.
     * 
     * @param socket The web socket session. If null, the session is derived from the current
     *            execution context.
     * @param invocations The client invocation requests.
     */
    public static void send(WebSocketSession socket, Iterable<ClientInvocation> invocations) {
        StringBuilder sb = null;
        
        try {
            for (ClientInvocation invocation : invocations) {
                sb = sb == null ? new StringBuilder() : sb;
                String json = writer.writeValueAsString(invocation.toMap());
                sb.append(sb.length() == 0 ? "[" : ",").append(json);
            }
            
            if (sb != null) {
                sb.append("]");
                sendData(socket, sb.toString());
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    /**
     * Sends an exception to the client for display via the web socket session.
     * 
     * @param socket The web socket session. If null, the session is derived from the current
     *            execution context.
     * @param exception The exception.
     */
    public static void sendError(WebSocketSession socket, Throwable exception) {
        if (exception instanceof InvocationTargetException) {
            exception = ((InvocationTargetException) exception).getTargetException();
        }
        
        try (StringWriter writer = new StringWriter(); PrintWriter print = new PrintWriter(writer);) {
            exception.printStackTrace(print);
            ClientInvocation invocation = new ClientInvocation(null, "cwf.alert", writer.toString(), "Error", "danger");
            send(socket, invocation);
        } catch (Exception e) {
            log.error("Could not send exception to client.", exception);
        }
    }
    
    protected static void notifySessionTrackers(Session session, boolean created) {
        for (ISessionTracker tracker : sessionTrackers) {
            if (created) {
                tracker.onSessionCreate(session);
            } else {
                tracker.onSessionDestroy(session);
            }
        }
    }
    
    /**
     * Processes a client request sent via the web socket session. Extracts the client request from
     * the message, creates a new execution context, and invokes registered request handlers. If no
     * registered request handler is capable of processing the request, an exception will be sent to
     * the client.
     * 
     * @param socket The web socket session transmitting the request.
     * @param message The message containing the client request.
     */
    @Override
    public void handleTextMessage(WebSocketSession socket, TextMessage message) {
        Session session = sessions.get(socket.getId());
        
        if (session == null) {
            throw new RuntimeException("Request received on unknown socket.");
        }
        
        Map<String, Object> attribs = socket.getAttributes();
        
        try {
            session.updateLastActivity();
            StringBuffer buffer = (StringBuffer) attribs.get(ATTR_BUFFER);
            String payload = message.getPayload();
            
            if (!message.isLast()) {
                if (buffer == null) {
                    attribs.put(ATTR_BUFFER, buffer = new StringBuffer(payload));
                } else {
                    buffer.append(payload);
                }
                
                return;
            }
            
            if (buffer != null) {
                payload = buffer.append(payload).toString();
                buffer = null;
                attribs.remove(ATTR_BUFFER);
                
                if (log.isWarnEnabled()) {
                    log.warn("Large payload received from client (" + payload.length() + " bytes).");
                }
            }
            
            Map<String, Object> map = reader.readValue(payload);
            session.init((String) map.get("pid"));
            ClientRequest request = new ClientRequest(session, map);
            processRequest(request);
            
        } catch (Exception e) {
            attribs.remove(ATTR_BUFFER);
            log.error("Error processing client request.", e);
            sendError(socket, e);
        }
    }
    
    private void processRequest(ClientRequest request) throws Exception {
        IRequestHandler handler = handlers.get(request.getType());
        
        if (handler == null) {
            throw new Exception("No registered handler for request type: " + request.getType());
        }
        
        ExecutionContext.clear();
        ExecutionContext.put(ExecutionContext.ATTR_REQUEST, request);
        
        try {
            handler.handleRequest(request);
            request.getPage().getEventQueue().processAll();
        } catch (Exception e) {
            request.getPage().getEventQueue().clearAll();
            log.error(e);
            throw e;
        } finally {
            ExecutionContext.clear();
        }
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession socket) throws Exception {
        Session session = new Session(servletContext, socket);
        sessions.put(session.getId(), session);
        
        if (log.isDebugEnabled()) {
            logSessionEvent(session, "established");
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) throws Exception {
        Session session = sessions.remove(socket.getId());
        
        if (session != null) {
            if (log.isDebugEnabled()) {
                logSessionEvent(session, "closed, " + status);
            }
            
            notifySessionTrackers(session, false);
            session.destroy();
        }
    }
    
    private void logSessionEvent(Session session, String event) {
        log.debug("Session #" + session.getId() + " " + event + ".");
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return true;
    }
    
    /**
     * NOP
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    /**
     * Detects and registers request handlers.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IRequestHandler) {
            registerRequestHandler((IRequestHandler) bean);
        } else if (bean instanceof ISessionTracker) {
            registerSessionTracker((ISessionTracker) bean);
        }
        
        return bean;
    }
    
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
        String debug = servletContext.getInitParameter("cwf-debug");
        ClientUtil.debugEnabled = BooleanUtils.toBoolean(debug);
    }
    
}
