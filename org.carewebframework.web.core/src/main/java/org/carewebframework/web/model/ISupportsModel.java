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

public interface ISupportsModel<T extends BaseComponent> {
    
    IModelAndView<T, ?> getModelAndView();
    
    @SuppressWarnings("unchecked")
    default <M> IModelAndView<T, M> getModelAndView(Class<M> clazz) {
        return (IModelAndView<T, M>) getModelAndView();
    }
    
    default IListModel<?> getModel() {
        return getModelAndView().getModel();
    }
    
    default <M> IListModel<M> getModel(Class<M> clazz) {
        return getModelAndView(clazz).getModel();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default <M> void setModel(IListModel<M> model) {
        getModelAndView().setModel((ListModel) model);
    }
    
    default IComponentRenderer<T, ?> getRenderer() {
        return getModelAndView().getRenderer();
    }
    
    default <M> IComponentRenderer<T, M> getRenderer(Class<M> clazz) {
        return getModelAndView(clazz).getRenderer();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    default <M> void setRenderer(IComponentRenderer<T, M> renderer) {
        getModelAndView().setRenderer((IComponentRenderer) renderer);
    }
}
