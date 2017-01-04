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
import org.springframework.util.Assert;

@Component(value = "upload", widgetPackage = "cwf-upload", widgetClass = "Upload", parentTag = "*")
public class Upload extends BaseUIComponent {
    
    private boolean multiple;
    
    private String accept;
    
    private int maxsize = 1024 * 1024 * 100;
    
    public Upload() {
        super();
    }
    
    @PropertyGetter("multiple")
    public boolean isMultiple() {
        return multiple;
    }
    
    @PropertySetter("multiple")
    public void setMultiple(boolean multiple) {
        if (multiple != this.multiple) {
            sync("multiple", this.multiple = multiple);
        }
    }
    
    @PropertyGetter("accept")
    public String getAccept() {
        return accept;
    }
    
    @PropertySetter("accept")
    public void setAccept(String accept) {
        if (!areEqual(accept = nullify(accept), this.accept)) {
            sync("accept", this.accept = accept);
        }
    }
    
    @PropertyGetter("maxsize")
    public int getMaxsize() {
        return maxsize;
    }
    
    @PropertySetter("maxsize")
    public void setMaxsize(int maxsize) {
        if (maxsize != this.maxsize) {
            Assert.isTrue(maxsize >= 0, "maxsize must be >= 0");
            sync("_maxsize", this.maxsize = maxsize);
        }
    }
    
    public void abortAll() {
        invokeIfAttached("abortAll");
    }
    
    public void abort(String filename) {
        invokeIfAttached("abort", filename);
    }
    
    public void bind(BaseUIComponent comp) {
        invoke("bind", comp);
    }
    
    public void unbind(BaseUIComponent comp) {
        invoke("unbind", comp);
    }
}
