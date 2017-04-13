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
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

/**
 * Component wrapping script source code for client-side invocation.
 */
@Component(value = "cscript", widgetClass = "Script", content = ContentHandling.AS_ATTRIBUTE, parentTag = "*")
public class ClientScript extends BaseSourcedComponent {

    private String type;

    public ClientScript() {
        super(true);
    }

    public ClientScript(String script) {
        super(script, true);
    }

    @PropertyGetter("type")
    public String getType() {
        return type;
    }

    @PropertySetter("type")
    public void setType(String type) {
        if (!areEqual(type = nullify(type), this.type)) {
            sync("type", this.type = type);
        }
    }

}
