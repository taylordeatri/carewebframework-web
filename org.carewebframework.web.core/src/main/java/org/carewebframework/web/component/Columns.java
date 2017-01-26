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

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;

@Component(value = "columns", widgetPackage = "cwf-table", widgetClass = "Columns", parentTag = "table", childTag = @ChildTag("column"))
public class Columns extends BaseUIComponent {
    
    private Column sortColumn;
    
    public Column getSortColumn() {
        return sortColumn;
    }
    
    public void setSortColumn(Column sortColumn) {
        if (sortColumn != this.sortColumn) {
            validateIsChild(sortColumn);
            
            if (this.sortColumn != null) {
                this.sortColumn._setSortColumn(false, false);
            }
            
            this.sortColumn = sortColumn;
            
            if (sortColumn != null) {
                sortColumn._setSortColumn(true, false);
            }
        }
    }
    
    @Override
    protected void afterAddChild(BaseComponent child) {
        super.afterAddChild(child);
        
        if (((Column) child).isSortColumn()) {
            setSortColumn((Column) child);
        }
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        super.afterRemoveChild(child);
        
        if (child == sortColumn) {
            sortColumn = null;
        }
    }
}
