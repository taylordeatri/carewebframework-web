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

import java.util.List;

/**
 * Describes a tracked list used to hold model objects.
 *
 * @param <T> The type of the model object.
 */
public interface IListModel<T> extends List<T> {
    
    interface IListModelListener {
        
        void onListChange(ListEventType type, int startIndex, int endIndex);
    }
    
    enum ListEventType {
        ADD, DELETE, REPLACE, SWAP, CHANGE
    }
    
    boolean addEventListener(IListModelListener listener);
    
    void removeAllListeners();
    
    boolean removeEventListener(IListModelListener listener);
    
    void swap(int index1, int index2);
    
    void swap(T value1, T value2);
    
    void sort(boolean ascending);
}
