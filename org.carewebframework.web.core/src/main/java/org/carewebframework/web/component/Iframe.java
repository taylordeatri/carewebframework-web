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

@Component(value = "iframe", widgetClass = "Iframe", parentTag = "*")
public class Iframe extends BaseUIComponent {
    
    private String src;
    
    private String sandbox;
    
    @PropertyGetter("src")
    public String getSrc() {
        return src;
    }
    
    @PropertySetter("src")
    public void setSrc(String src) {
        if (!areEqual(src = nullify(src), this.src)) {
            sync("src", this.src = src);
        }
    }
    
    @PropertyGetter("sandbox")
    public String getSandbox() {
        return sandbox;
    }
    
    @PropertySetter("sandbox")
    public void setSandbox(String sandbox) {
        if (!areEqual(sandbox, this.sandbox)) {
            sync("sandbox", this.sandbox = sandbox);
        }
    }
}
