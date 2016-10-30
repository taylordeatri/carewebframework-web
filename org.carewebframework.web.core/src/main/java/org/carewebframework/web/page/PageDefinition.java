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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.annotation.ComponentDefinition.DeferredSetter;
import org.carewebframework.web.annotation.ComponentDefinition.FactoryContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.expression.ELEvaluator;

public class PageDefinition {
    
    private final PageElement root = new PageElement(null, null);
    
    public PageElement getRootElement() {
        return root;
    }
    
    public List<BaseComponent> materialize(BaseComponent parent) throws Exception {
        return materialize(parent, null);
    }
    
    public List<BaseComponent> materialize(BaseComponent parent, Map<String, Object> args) throws Exception {
        List<DeferredSetter> deferrals = new ArrayList<>();
        List<BaseComponent> created = new ArrayList<>();
        materialize(root.getChildren(), parent, deferrals, created);
        
        if (args != null) {
            for (BaseComponent component : created) {
                component.getAttributes().putAll(args);
            }
        }
        
        for (DeferredSetter deferral : deferrals) {
            deferral.execute();
        }
        
        return created;
    }
    
    private void materialize(Iterable<PageElement> children, BaseComponent parent, List<DeferredSetter> deferrals,
                             List<BaseComponent> created) throws Exception {
        if (children != null) {
            for (PageElement child : children) {
                BaseComponent component = materialize(child, parent, deferrals);
                
                if (created != null) {
                    created.add(component);
                }
            }
        }
    }
    
    private BaseComponent materialize(PageElement element, BaseComponent parent,
                                      List<DeferredSetter> deferrals) throws Exception {
        ComponentDefinition def = element.getDefinition();
        boolean merge = parent instanceof Page && def.getComponentClass() == Page.class;
        boolean skip = def.getComponentClass() == Page.class && parent != null;
        Map<String, String> attributes;
        BaseComponent component;
        
        if (merge) {
            component = parent;
            parent = null;
            attributes = element.getAttributes();
        } else if (skip) {
            component = parent;
            parent = null;
            attributes = null;
        } else {
            FactoryContext context = new FactoryContext(element);
            component = def.create(context);
            
            if (component == null) {
                return null;
            }
            
            attributes = context.getAttributes();
        }
        
        if (attributes != null) {
            for (Entry<String, String> attribute : attributes.entrySet()) {
                Object value = ELEvaluator.getInstance().evaluate(attribute.getValue(), component);
                DeferredSetter deferral = def.setProperty(component, attribute.getKey(), value);
                
                if (deferral != null) {
                    deferrals.add(deferral);
                }
            }
        }
        
        if (parent != null) {
            parent._addChild(component, -1, true);
        }
        
        materialize(element.getChildren(), component, deferrals, null);
        return component;
    }
}
