package org.carewebframework.web.component;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;

@Component(value = "columns", widgetPackage = "cwf-table", widgetClass = "Columns", parentTag = "table", childTag = @ChildTag("column"))
public class Columns extends BaseUIComponent {
    
}