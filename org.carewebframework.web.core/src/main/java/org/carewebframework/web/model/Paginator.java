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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Supports paging within a list model.
 */
public class Paginator implements IPaginator {

    private final List<IPagingListener> listeners = new ArrayList<>();

    private int pageSize;

    private int currentPage;

    private int modelSize;

    public Paginator() {
    }
    
    @Override
    public int getPageSize() {
        return pageSize;
    }
    
    @Override
    public void setPageSize(int pageSize) {
        if (pageSize != this.pageSize) {
            currentPage = 0;
            fireEvent(PagingEventType.PAGE_SIZE, this.pageSize, this.pageSize = pageSize);
        }
    }
    
    @Override
    public int getCurrentPage() {
        return currentPage;
    }
    
    @Override
    public void setCurrentPage(int pageIndex) {
        if (pageIndex != currentPage) {
            Assert.isTrue(pageIndex >= 0, "Current page may not be less than 0");
            Assert.isTrue(pageIndex <= getMaxPage(), "Current page may not exceed maximum number of pages");
            fireEvent(PagingEventType.CURRENT_PAGE, this.currentPage, this.currentPage = pageIndex);
        }
    }
    
    @Override
    public int getModelSize() {
        return modelSize;
    }
    
    public void setModelSize(int modelSize) {
        if (modelSize != this.modelSize) {
            Assert.isTrue(modelSize >= 0, "Model size must not be less than 0");
            int oldmax = getMaxPage();
            this.modelSize = modelSize;
            fireEvent(PagingEventType.MAX_PAGE, oldmax, getMaxPage());
        }
    }

    @Override
    public int getMaxPage() {
        return isDisabled() || modelSize == 0 ? 0 : (modelSize - 1) / pageSize;
    }

    public boolean inRange(int modelIndex) {
        return isDisabled() || (modelIndex >= getModelOffset(currentPage) && modelIndex < getModelOffset(currentPage + 1));
    }

    public int getModelOffset(int pageIndex) {
        return isDisabled() ? 0 : Math.min(pageIndex * pageSize, modelSize);
    }
    
    @Override
    public boolean addEventListener(IPagingListener listener) {
        return listeners.add(listener);
    }
    
    @Override
    public void removeAllListeners() {
        listeners.clear();
    }
    
    @Override
    public boolean removeEventListener(IPagingListener listener) {
        return listeners.remove(listener);
    }

    private void fireEvent(PagingEventType type, int oldValue, int newValue) {
        for (IPagingListener listener : listeners) {
            listener.onPagingChange(type, oldValue, newValue);
        }
    }
    
}
