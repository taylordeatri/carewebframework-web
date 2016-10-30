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

import org.carewebframework.common.NumUtil;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

public abstract class BaseInputboxComponent<T> extends BaseInputComponent<T> {
    
    private String placeholder;
    
    private int maxLength;
    
    private boolean readonly;
    
    private boolean synced;
    
    protected boolean getSynchronized() {
        return synced;
    }
    
    protected void setSynchronized(boolean synchronize) {
        if (synchronize != this.synced) {
            sync("synced", this.synced = synchronize);
        }
    }
    
    @PropertyGetter("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }
    
    @PropertySetter("placeholder")
    public void setPlaceholder(String placeholder) {
        if (!areEqual(placeholder = nullify(placeholder), this.placeholder)) {
            sync("placeholder", this.placeholder = placeholder);
        }
    }
    
    @PropertyGetter("maxlength")
    public int getMaxLength() {
        return maxLength;
    }
    
    @PropertySetter("maxlength")
    public void setMaxLength(int maxLength) {
        maxLength = NumUtil.enforceRange(maxLength, 0, 524288);
        
        if (maxLength != this.maxLength) {
            sync("maxlength", this.maxLength = maxLength);
        }
    }
    
    @PropertyGetter("readonly")
    public boolean isReadonly() {
        return readonly;
    }
    
    @PropertySetter("readonly")
    public void setReadonly(boolean readonly) {
        if (readonly != this.readonly) {
            sync("readonly", this.readonly = readonly);
        }
    }
    
    public void selectAll() {
        invoke("selectAll");
    }
    
    public void selectRange(int start, int end) {
        invoke("selectRange", start, end);
    }
}
