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
 * Interface for components that support an associated model and view. Note that default
 * implementations are provided for all but one method. Therefore, a component only needs to
 * implement that one method.
 *
 * @param <T> The type of component rendered from the model.
 */
public interface ISupportsModel<T extends BaseComponent> {

    /**
     * Returns the model and view for this component.
     *
     * @return The model and view for this component.
     */
    IModelAndView<T, ?> getModelAndView();

    /**
     * Returns the model and view for this component. The model is cast to the specified type.
     *
     * @param type The type of model object.
     * @return The model and view for this component.
     */
    @SuppressWarnings("unchecked")
    default <M> IModelAndView<T, M> getModelAndView(Class<M> type) {
        return (IModelAndView<T, M>) getModelAndView();
    }

    /**
     * @see IModelAndView#getModel()
     */
    @SuppressWarnings("javadoc")
    default IListModel<?> getModel() {
        return getModelAndView().getModel();
    }

    /**
     * Returns the model for this component. The model is cast to the specified type.
     *
     * @param type The type of model object.
     * @return The model this component.
     */
    default <M> IListModel<M> getModel(Class<M> type) {
        return getModelAndView(type).getModel();
    }

    /**
     * @see IModelAndView#setModel(IListModel)
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "javadoc" })
    default <M> void setModel(IListModel<M> model) {
        getModelAndView().setModel((ListModel) model);
    }

    /**
     * @see IModelAndView#getRenderer()
     */
    @SuppressWarnings("javadoc")
    default IComponentRenderer<T, ?> getRenderer() {
        return getModelAndView().getRenderer();
    }

    /**
     * @see IModelAndView#getRenderer(Class)
     */
    @SuppressWarnings("javadoc")
    default <M> IComponentRenderer<T, M> getRenderer(Class<M> type) {
        return getModelAndView(type).getRenderer();
    }

    /**
     * @see IModelAndView#setRenderer(IComponentRenderer)
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "javadoc" })
    default <M> void setRenderer(IComponentRenderer<T, M> renderer) {
        getModelAndView().setRenderer((IComponentRenderer) renderer);
    }

    /**
     * @see IModelAndView#getDeferredRendering
     */
    @SuppressWarnings("javadoc")
    default boolean getDeferredRendering() {
        return getModelAndView().getDeferredRendering();
    }

    /**
     * @see IModelAndView#setDeferredRendering(boolean)
     */
    @SuppressWarnings("javadoc")
    default void setDeferredRendering(boolean value) {
        getModelAndView().setDeferredRendering(value);
    }

    /**
     * @see IModelAndView#getPaginator()
     */
    @SuppressWarnings("javadoc")
    default IPaginator getPaginator() {
        return getModelAndView().getPaginator();
    }
}
