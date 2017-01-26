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
package org.carewebframework.web.expression;

import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.page.PageElement;

/**
 * This serves as the context root for an EL expression evaluation.
 */
public class ELContext {
    
    private final BaseComponent component;
    
    private final BaseComponent parent;
    
    private final PageElement element;
    
    public ELContext(BaseComponent component, BaseComponent parent, PageElement element) {
        this.component = component;
        this.parent = parent;
        this.element = element;
    }
    
    public Object getValue(String name) {
        Object result = "self".equals(name) ? component : element.getTagLibrary(name);
        result = result != null ? result : component.getAttribute(name);
        result = result != null ? result : parent == null ? null : parent.findByName(name);
        return result;
    }
}
