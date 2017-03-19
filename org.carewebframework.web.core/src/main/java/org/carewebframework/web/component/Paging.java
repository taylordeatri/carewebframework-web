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
package org.carewebframework.web.component;

import org.carewebframework.web.ancillary.ComponentException;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.model.IPaginator;
import org.carewebframework.web.model.IPaginator.IPagingListener;
import org.carewebframework.web.model.ISupportsModel;

/**
 * A page navigation component.
 */
@Component(value = "paging", widgetClass = "Paging", parentTag = "*")
public class Paging extends BaseLabeledComponent<BaseLabeledComponent.LabelPositionNone> {

    private IPaginator paginator;

    private int currentPage;
    
    private int pageSize;

    private int maxPage;

    private boolean fromPaginator;
    
    private final IPagingListener pagingListener = (type, oldValue, newValue) -> {
        try {
            fromPaginator = true;

            switch (type) {
                case CURRENT_PAGE:
                    setCurrentPage(newValue);
                    break;
                
                case PAGE_SIZE:
                    setPageSize(newValue);
                    break;
                
                case MAX_PAGE:
                    setMaxPage(newValue);
                    break;
            }
        } finally {
            fromPaginator = false;
        }
    };
    
    public Paging() {
        this(null);
    }

    public Paging(String label) {
        super(label);
    }
    
    public IPaginator getPaginator() {
        return paginator;
    }
    
    public void setPaginator(IPaginator paginator) {
        if (paginator != this.paginator) {
            if (this.paginator != null) {
                this.paginator.removeEventListener(pagingListener);
            }
            
            this.paginator = paginator;
            setMaxPage(paginator == null ? 0 : paginator.getMaxPage());
            syncToPaginator();
        }
    }

    @PropertyGetter("currentPage")
    public int getCurrentPage() {
        return currentPage;
    }

    @PropertySetter("currentPage")
    public void setCurrentPage(int currentPage) {
        if (currentPage != this.currentPage) {
            sync("currentPage", this.currentPage = currentPage);
            syncToPaginator();
        }
    }

    @PropertyGetter("pageSize")
    public int getPageSize() {
        return pageSize;
    }

    @PropertySetter("pageSize")
    public void setPageSize(int pageSize) {
        if (pageSize != this.pageSize) {
            sync("pageSize", this.pageSize = pageSize);
            syncToPaginator();
        }
    }
    
    @PropertySetter(value = "target", defer = true)
    private void setPagingTarget(BaseComponent comp) {
        if (comp == null) {
            setPaginator(null);
        } else if (comp instanceof ISupportsModel) {
            setPaginator(((ISupportsModel<?>) comp).getPaginator());
        } else {
            throw new ComponentException(comp, "Paging target does not support model");
        }
    }

    private void setMaxPage(int maxPage) {
        if (maxPage != this.maxPage) {
            sync("maxPage", this.maxPage = maxPage);
        }
    }

    private void syncToPaginator() {
        if (paginator != null && !fromPaginator) {
            paginator.removeEventListener(pagingListener);
            paginator.setPageSize(pageSize);
            paginator.setCurrentPage(currentPage);
            paginator.addEventListener(pagingListener);
        }
    }
    
    @EventHandler(value = "change", syncToClient = false)
    private void _onChange(ChangeEvent event) {
        currentPage = defaultify(event.getValue(Integer.class), currentPage);
        syncToPaginator();
    }
}
