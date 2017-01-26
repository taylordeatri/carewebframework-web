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
package org.carewebframework.web.model;

import org.carewebframework.web.component.BaseComponent;

/**
 * This is a composite class consisting of a list model and its associated renderer.
 *
 * @param <T> The component type to be rendered.
 * @param <M> The model type.
 */
public interface IModelAndView<T extends BaseComponent, M> {
    
    /**
     * Returns the list model, or null if none set.
     * 
     * @return The list model, possibly null.
     */
    IListModel<M> getModel();
    
    /**
     * Sets the list model. If not null and a renderer has been set, the model will be re-rendered
     * immediately. If null, any previous rendering will be removed.
     * 
     * @param model The list model, or null to remove an existing one.
     */
    void setModel(IListModel<M> model);
    
    /**
     * Returns the renderer, or null if none set.
     * 
     * @return The renderer, possibly null.
     */
    IComponentRenderer<T, M> getRenderer();
    
    /**
     * Sets the renderer. If not null and a model has been set, the model will be re-rendered
     * immediately. If null, any previous rendering will be removed.
     * 
     * @param renderer The renderer, or null to remove an existing one.
     */
    void setRenderer(IComponentRenderer<T, M> renderer);
    
    /**
     * Force a re-rendering of the model. If either a model or a renderer has not been set, this
     * will have no effect.
     */
    void rerender();
    
    /**
     * Re-render a specific model object.
     * 
     * @param object The model object to re-render.
     * @return The rendered model object.
     */
    T rerender(M object);
    
    /**
     * Re-render a specific model object given its index.
     * 
     * @param index The index of the model object to re-render.
     * @return The rendered model object.
     */
    T rerender(int index);
}
