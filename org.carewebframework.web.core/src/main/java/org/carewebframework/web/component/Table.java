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

@Component(value = "table", widgetPackage = "cwf-table", widgetClass = "Table", parentTag = "*", childTag = {
        @ChildTag(value = "rows", maximum = 1), @ChildTag(value = "columns", maximum = 1) })
public class Table extends BaseUIComponent {
    
    @Component(value = "rows", widgetPackage = "cwf-table", widgetClass = "Rows", parentTag = "table", childTag = @ChildTag("row"))
    public static class Rows extends BaseUIComponent {
        
    }
    
    @Component(value = "columns", widgetPackage = "cwf-table", widgetClass = "Columns", parentTag = "table", childTag = @ChildTag("column"))
    public static class Columns extends BaseUIComponent {
        
    }
    
    private Columns columns;
    
    private Rows rows;
    
    public Table() {
        addClass("table table-bordered");
    }
    
    @Override
    public void afterAddChild(BaseComponent child) {
        super.afterAddChild(child);
        
        if (child instanceof Rows) {
            rows = (Rows) child;
        }
        
        if (child instanceof Columns) {
            columns = (Columns) child;
        }
    }
    
    @Override
    public void afterRemoveChild(BaseComponent child) {
        super.afterRemoveChild(child);
        
        if (child == rows) {
            rows = null;
        } else if (child == columns) {
            columns = null;
        }
    }
    
    public Columns getColumns() {
        return columns;
    }
    
    public Rows getRows() {
        return rows;
    }
}