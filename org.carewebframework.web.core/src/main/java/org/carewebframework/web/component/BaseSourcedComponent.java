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

import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * Base class for components that allow content to be expressed inline or imported from an external
 * source.
 */
public abstract class BaseSourcedComponent extends BaseComponent {

    private String src;

    public BaseSourcedComponent(boolean contentSynced) {
        this(null, contentSynced);
    }

    public BaseSourcedComponent(String content, boolean contentSynced) {
        setContentSynced(contentSynced);
        setContent(content);
    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    @Override
    public void setContent(String content) {
        content = nullify(content);

        if (content != null) {
            setSrc(null);
        }

        super.setContent(content);
    }
    
    @PropertyGetter("src")
    public String getSrc() {
        return src;
    }

    @PropertySetter(value = "src")
    public void setSrc(String src) {
        src = nullify(src);

        if (src != null) {
            super.setContent(null);
        }

        if (!areEqual(src, this.src)) {
            this.src = src;
            
            if (isContentSynced()) {
                sync("src", src);
            }
        }
    }
    
}
