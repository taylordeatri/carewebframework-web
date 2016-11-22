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
