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
package org.carewebframework.web.page;

import java.util.Collection;

import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.web.component.Page;
import org.springframework.util.Assert;

/**
 * A registry of all active pages, indexed by their page id.
 */
public class PageRegistry extends AbstractRegistry<String, Page> {
    
    private static final PageRegistry instance = new PageRegistry();
    
    public static void registerPage(Page page) {
        instance.register(page);
    }
    
    public static void unregisterPage(Page page) {
        instance.unregister(page);
    }
    
    public static Page getPage(String pid) {
        Page page = instance.get(pid);
        Assert.notNull(page, "Page not found: " + pid);
        return page;
    }
    
    public static Collection<Page> getPages() {
        return instance.getAll();
    }
    
    private PageRegistry() {
    }
    
    @Override
    protected String getKey(Page item) {
        return item.getId();
    }
    
}
