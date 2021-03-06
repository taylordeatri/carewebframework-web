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
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/*
 * A single pane within a pane view.
 */
@Component(value = "pane", widgetModule = "cwf-paneview", widgetClass = "Pane", content = ContentHandling.AS_CHILD, parentTag = "paneview", childTag = @ChildTag("*"))
public class Pane extends BaseUIComponent {
    
    private boolean splittable;
    
    private String title;
    
    @PropertyGetter("splittable")
    public boolean isSplittable() {
        return splittable;
    }
    
    @PropertySetter("splittable")
    public void setSplittable(boolean splittable) {
        if (splittable != this.splittable) {
            sync("splittable", this.splittable = splittable);
        }
    }
    
    @PropertyGetter("title")
    public String getTitle() {
        return title;
    }
    
    @PropertySetter("title")
    public void setTitle(String title) {
        if (!areEqual(title = nullify(title), this.title)) {
            sync("title", this.title = title);
        }
    }
    
}
