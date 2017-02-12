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
import org.carewebframework.web.expression.ELContext;
import org.carewebframework.web.expression.ELEvaluator;

/**
 * This represents the compiled form of a single cwf page. It is a simple wrapper of a tree of page
 * elements, rooted at the root element.
 */
public class PageDefinition {
    
    private final PageElement root = new PageElement(null, null);
    
    /**
     * The root of all page elements in this definition.
     * 
     * @return The root page element.
     */
    public PageElement getRootElement() {
        return root;
    }
    
    /**
     * Materializes this page definition under the given parent component.
     * 
     * @param parent The parent component for all top level components produced. This may be null.
     * @return A list of all top level components produced.
     */
    public List<BaseComponent> materialize(BaseComponent parent) {
        return materialize(parent, null);
    }
    
    /**
     * Materializes this page definition under the given parent component.
     * 
     * @param parent The parent component for all top level components produced. This may be null.
     * @param args A map of arguments that will be copied into the attribute maps of all top level
     *            components. This may be null.
     * @return A list of all top level components produced.
     */
    public List<BaseComponent> materialize(BaseComponent parent, Map<String, Object> args) {
        List<DeferredSetter> deferrals = new ArrayList<>();
        List<BaseComponent> created = new ArrayList<>();
        materialize(root.getChildren(), parent, deferrals, created);
        
        if (args != null && !args.isEmpty()) {
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
                             List<BaseComponent> created) {
        if (children != null) {
            for (PageElement child : children) {
                BaseComponent component = materialize(child, parent, deferrals);
                
                if (created != null) {
                    created.add(component);
                }
            }
        }
    }
    
    private BaseComponent materialize(PageElement element, BaseComponent parent, List<DeferredSetter> deferrals) {
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
            ELContext elContext = new ELContext(component, parent, element);
            
            for (Entry<String, String> attribute : attributes.entrySet()) {
                Object value = ELEvaluator.getInstance().evaluate(attribute.getValue(), elContext);
                DeferredSetter deferral = def.setProperty(component, attribute.getKey(), value);
                
                if (deferral != null) {
                    deferrals.add(deferral);
                }
            }
        }
        
        materialize(element.getChildren(), component, deferrals, null);
        
        if (parent != null) {
            parent.addChild(component);
        }
        
        return component;
    }
}
