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
package org.carewebframework.web.ancillary;

import org.carewebframework.web.component.BaseComponent;
import org.springframework.core.NestedRuntimeException;

public class ComponentException extends NestedRuntimeException {

    private static final long serialVersionUID = 1L;

    private final BaseComponent component;

    private final Class<? extends BaseComponent> componentClass;

    private static String formatMessage(Object object, String message, Object... args) {
        return (object == null ? "" : object + ": ") + String.format(message, args);
    }

    public ComponentException(String message, Object... args) {
        super(formatMessage(null, message, args));
        this.component = null;
        this.componentClass = null;
    }

    public ComponentException(Class<? extends BaseComponent> componentClass, String message, Object... args) {
        super(formatMessage(componentClass, message, args));
        this.componentClass = componentClass;
        this.component = null;
    }

    public ComponentException(BaseComponent component, String message, Object... args) {
        super(formatMessage(component, message, args));
        this.component = component;
        this.componentClass = component == null ? null : component.getClass();
    }

    public BaseComponent getComponent() {
        return component;
    }

    public Class<? extends BaseComponent> getComponentClass() {
        return componentClass;
    }

}
