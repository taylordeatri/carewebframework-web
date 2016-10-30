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

import org.carewebframework.api.spring.ScopeContainer;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.Page;

/**
 * Implements a custom Spring scope based on the ZK page.
 */
public class PageScope extends AbstractScope<Page> {
    
    public PageScope() {
        super(true);
        //LifecycleEventDispatcher.addPageCallback(this);
    }
    
    @Override
    protected ScopeContainer getScopeContainer(Page scope) {
        return (ScopeContainer) scope.getAttribute(getKey());
    }
    
    @Override
    protected void bindContainer(Page scope, ScopeContainer container) {
        scope.setAttribute(getKey(), container);
        container.setConversationId(scope.getId());
    }
    
    @Override
    protected Page getActiveScope() {
        return ExecutionContext.getPage();
    }
    
    public void onInit(Page page) {
        getContainer(page, false);
    }
    
    public void onCleanup(Page page) {
        ScopeContainer container = getContainer(page, false);
        
        if (container != null) {
            container.destroy();
            page.removeAttribute(getKey());
        }
    }
    
}
