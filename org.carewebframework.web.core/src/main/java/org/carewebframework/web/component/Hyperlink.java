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
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

@Component(value = "a", widgetClass = "Hyperlink", parentTag = "*")
public class Hyperlink extends BaseLabeledImageComponent {
    
    private String href;
    
    private String target;
    
    public Hyperlink() {
        addClass("btn-link");
    }
    
    @PropertyGetter("href")
    public String getHref() {
        return href;
    }
    
    @PropertySetter("href")
    public void setHref(String href) {
        if (!areEqual(href = nullify(href), this.href)) {
            sync("href", this.href = href);
        }
    }
    
    @PropertyGetter("target")
    public String getTarget() {
        return target;
    }
    
    @PropertySetter("target")
    public void setTarget(String target) {
        if (!areEqual(href = nullify(target), this.target)) {
            sync("target", this.target = target);
        }
    }
    
}
