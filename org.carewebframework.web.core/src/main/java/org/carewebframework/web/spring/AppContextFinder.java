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
package org.carewebframework.web.spring;

import org.carewebframework.api.spring.IAppContextFinder;
import org.carewebframework.api.spring.SpringUtil;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * Encapsulates the logic for locating the application context within a web environment.
 */
public class AppContextFinder implements IAppContextFinder {
    
    protected static ApplicationContext rootContext;
    
    public AppContextFinder() {
        SpringUtil.setAppContextFinder(this);
    }
    
    /**
     * Creates an application context as a child of the root application context and associates it
     * with the specified page. In this way, any objects managed by the root application context are
     * available to the framework context.
     * 
     * @param page Page for which application context is being created.
     * @return New application context
     */
    public static ApplicationContext createAppContext(Page page) {
        FrameworkAppContext appContext = new FrameworkAppContext(page);
        appContext.refresh();
        return appContext;
    }
    
    /**
     * Destroys the application context for the current page.
     */
    public static void destroyAppContext() {
        destroyAppContext(ExecutionContext.getPage());
    }
    
    /**
     * Destroys the application context associated with the specified page.
     * 
     * @param page Page instance
     */
    public static void destroyAppContext(Page page) {
        FrameworkAppContext appContext = FrameworkAppContext.getAppContext(page);
        
        if (appContext != null) {
            appContext.close();
        }
    }
    
    /**
     * Returns the application context for the current scope. If no page exists or no application
     * context is associated with the page, looks for an application context registered to the
     * current thread. Failing that, returns the root application context.
     * 
     * @see org.carewebframework.api.spring.IAppContextFinder#getAppContext()
     * @return An application context.
     */
    @Override
    public ApplicationContext getAppContext() {
        ApplicationContext appContext = FrameworkAppContext.getAppContext(ExecutionContext.getPage());
        return appContext == null ? getRootAppContext() : appContext;
    }
    
    /**
     * Returns the root application context.
     * 
     * @see org.carewebframework.api.spring.IAppContextFinder#getRootAppContext()
     * @return The root application context.
     */
    @Override
    public ApplicationContext getRootAppContext() {
        return rootContext != null ? rootContext : ContextLoader.getCurrentWebApplicationContext();
    }
}
