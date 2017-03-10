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
package org.carewebframework.web.test;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.event.EventQueue;
import org.carewebframework.web.spring.ClasspathMessageSource;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This class creates a mock CWF environment suitable for certain kinds of unit tests. It creates a
 * web app instance with a single page and desktop and a mock session and execution. It also creates
 * a root Spring application context with a child desktop context.
 */
public class MockEnvironment {

    private MockSession session;

    private MockClientRequest clientRequest;

    private MockServletContext servletContext;

    private XmlWebApplicationContext rootContext;

    private final Map<String, Object> browserInfo = new HashMap<>();

    private final Map<String, Object> clientRequestMap = new HashMap<>();

    /**
     * Creates a mock environment for unit testing.
     */
    public MockEnvironment() {
    }

    /**
     * Initializes the mock environment.
     *
     * @param profiles Active profiles.
     * @param configLocations Additional config file locations.
     * @throws Exception Unspecified exception.
     */
    public void init(String[] profiles, String[] configLocations) throws Exception {
        // Set up web app
        servletContext = initServletContext(new MockServletContext());
        // Create root Spring context
        rootContext = initAppContext(profiles, configLocations);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, rootContext);
        rootContext.refresh();
        // Create mock session
        MockWebSocketSession socket = new MockWebSocketSession();
        session = new MockSession(servletContext, socket);
        // Create the mock request
        initBrowserInfoMap(browserInfo);
        clientRequestMap.put("data", browserInfo);
        initClientRequestMap(clientRequestMap);
        clientRequest = new MockClientRequest(session, clientRequestMap);
        // Initialize the page
        Page page = session.getPage();
        Page._init(page, clientRequest, session.getSynchronizer());
        page = initPage(page);
        // Create the mock execution
        initExecutionContext();
    }

    /**
     * Cleans up all application contexts and invalidates the session.
     */
    public void close() {
        session.destroy();
        rootContext.close();
    }

    protected XmlWebApplicationContext createApplicationContext() {
        return new XmlWebApplicationContext();
    }

    /**
     * Initialize the mock servlet context.
     *
     * @param servletContext The mock servlet context.
     * @return The initialized mock servlet context.
     */
    protected MockServletContext initServletContext(MockServletContext servletContext) {
        return servletContext;
    }

    protected void initExecutionContext() {
        ExecutionContext.put(ExecutionContext.ATTR_REQUEST, clientRequest);
    }

    protected void initClientRequestMap(Map<String, Object> map) {
        map.put("pid", session.getPage().getId());
        map.put("type", "mock");
    }

    /**
     * Initialize the app context.
     *
     * @param profiles Active profiles.
     * @param configLocations Optional configuration locations.
     * @return The initialized app context.
     * @throws Exception
     */
    protected XmlWebApplicationContext initAppContext(String[] profiles, String[] configLocations) throws Exception {

        XmlWebApplicationContext appContext = createApplicationContext();
        appContext.setServletContext(servletContext);
        ClasspathMessageSource.getInstance().setResourceLoader(appContext);

        if (configLocations != null) {
            appContext.setConfigLocations(configLocations);
        }

        if (profiles != null && profiles.length > 0) {
            appContext.getEnvironment().setActiveProfiles(profiles);
            appContext.getEnvironment().setDefaultProfiles(new String[] { profiles[0] });
        }

        return appContext;
    }

    /**
     * Initialize browserInfo map.
     *
     * @param browserInfo The browser info map.
     */
    protected void initBrowserInfoMap(Map<String, Object> browserInfo) {
    }

    /**
     * Initialize the page.
     *
     * @param page The page.
     * @return The initialized page.
     */
    protected Page initPage(Page page) {
        return page;
    }

    public ApplicationContext getRootContext() {
        return rootContext;
    }

    public MockSession getSession() {
        return session;
    }

    /**
     * Flushes and processes any event on the event queue.
     *
     * @return True if events were flushed.
     */
    public boolean flushEvents() {
        EventQueue queue = session.getPage().getEventQueue();
        boolean flushed = !queue.isEmpty();
        queue.processAll();
        return flushed;
    }

}
