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

/**
 * Extends the list model by adding support for nesting models within models.
 *
 * @param <M> The type of the model object.
 */
public interface INestedModel<M> extends IListModel<M> {
    
    /**
     * Returns the child list model given the parent. May return null. Note that the return type is
     * not required to be a nested model, though it typically will be.
     * 
     * @param parent Parent whose children are sought.
     * @return The child list model. May be null.
     */
    IListModel<M> getChildren(M parent);
}
