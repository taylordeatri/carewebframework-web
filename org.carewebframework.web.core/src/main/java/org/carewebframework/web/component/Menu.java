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
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.OpenEvent;

@Component(value = "menu", widgetClass = "Menu", parentTag = "*", childTag = { @ChildTag("menuitem"),
        @ChildTag("menuheader"), @ChildTag("menuseparator") })
public class Menu extends BaseLabeledImageComponent<BaseLabeledComponent.LabelPositionNone> {
    
    private boolean open;
    
    @PropertyGetter("open")
    public boolean isOpen() {
        return open;
    }
    
    @PropertySetter("open")
    public void setOpen(boolean open) {
        if (open != this.open) {
            sync("open", this.open = open);
        }
    }
    
    @EventHandler(value = { "open", "close" }, syncToClient = false)
    private void onOpenOrClose(Event event) {
        open = event instanceof OpenEvent;
    }
    
    public void open() {
        setOpen(true);
    }
    
    public void close() {
        setOpen(false);
    }
}
