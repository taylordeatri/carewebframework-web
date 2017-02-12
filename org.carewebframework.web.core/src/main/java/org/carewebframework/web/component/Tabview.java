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
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * A component supporting a tab-based view.
 */
@Component(value = "tabview", widgetPackage = "cwf-tabview", widgetClass = "Tabview", parentTag = "*", childTag = @ChildTag("tab"))
public class Tabview extends BaseUIComponent {
    
    public enum TabPosition {
        TOP, BOTTOM, LEFT, RIGHT
    }
    
    private Tab selectedTab;
    
    private TabPosition tabPosition = TabPosition.TOP;
    
    public Tab getSelectedTab() {
        return selectedTab;
    }
    
    public void setSelectedTab(Tab selectedTab) {
        validateIsChild(selectedTab);
        
        if (this.selectedTab != null) {
            this.selectedTab._setSelected(false, false);
        }
        
        this.selectedTab = selectedTab;
        
        if (selectedTab != null) {
            selectedTab._setSelected(true, false);
        }
    }
    
    @Override
    protected void afterRemoveChild(BaseComponent child) {
        if (child == selectedTab) {
            selectedTab = null;
        }
    }
    
    @Override
    protected void afterAddChild(BaseComponent child) {
        if (((Tab) child).isSelected()) {
            setSelectedTab((Tab) child);
        }
    }
    
    @PropertyGetter("tabPosition")
    public TabPosition getTabPosition() {
        return tabPosition;
    }
    
    @PropertySetter("tabPosition")
    public void setTabPosition(TabPosition tabPosition) {
        tabPosition = tabPosition == null ? TabPosition.TOP : tabPosition;
        
        if (tabPosition != this.tabPosition) {
            sync("tabPosition", this.tabPosition = tabPosition);
        }
    }
    
}
