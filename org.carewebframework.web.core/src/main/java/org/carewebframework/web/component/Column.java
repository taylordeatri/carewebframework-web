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

import java.util.Comparator;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.model.IListModel;
import org.carewebframework.web.model.SortOrder;

@Component(value = "column", widgetClass = "Column", widgetPackage = "cwf-table", parentTag = "columns", childTag = @ChildTag("*"))
public class Column extends BaseLabeledComponent {
    
    private Comparator<?> sortComparator;
    
    private SortOrder sortOrder;
    
    public Comparator<?> getSortComparator() {
        return sortComparator;
    }
    
    public void setSortComparator(Comparator<?> sortComparator) {
        if (sortComparator != this.sortComparator) {
            this.sortComparator = sortComparator;
            sort();
        }
    }
    
    @PropertyGetter("sortOrder")
    public SortOrder getSortOrder() {
        return sortOrder;
    }
    
    @PropertySetter("sortOrder")
    public void setSortOrder(SortOrder sortOrder) {
        if (sortOrder != this.sortOrder) {
            sync("sortOrder", this.sortOrder = sortOrder);
            sort();
        }
    }
    
    public void sort() {
        IListModel<Object> model = sortComparator == null || sortOrder == null ? null : getModel();
        
        if (model != null) {
            @SuppressWarnings("unchecked")
            Comparator<Object> comparator = sortOrder == null ? null : (Comparator<Object>) sortComparator;
            model.sort(comparator, sortOrder != SortOrder.DESCENDING);
        }
    }
    
    private IListModel<Object> getModel() {
        Table table = getAncestor(Table.class);
        Rows rows = table == null ? null : table.getRows();
        return rows == null ? null : rows.getModelAndView(Object.class).getModel();
    }
    
    @EventHandler("sort")
    private void _sort() {
        if (sortOrder != null) {
            int i = sortOrder.ordinal() + 1;
            setSortOrder(SortOrder.values()[i == 3 ? 0 : i]);
        }
    }
}
