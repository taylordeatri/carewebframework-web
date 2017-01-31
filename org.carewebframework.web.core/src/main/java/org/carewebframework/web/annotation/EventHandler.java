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
package org.carewebframework.web.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a handler method for one or more specific events.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(EventHandlers.class)
public @interface EventHandler {
    
    /**
     * @return The event type(s) to be handled.
     */
    String[] value();
    
    /**
     * @return The event target(s). If prefixed with an "@" character, the target is assumed to be
     *         the name of an instance variable (member field). Otherwise, it represents the name
     *         associated with the target component.
     */
    String[] target() default {};
    
    /**
     * @return Action to be taken if event handler cannot be wired.
     */
    OnFailure onFailure() default OnFailure.EXCEPTION;
    
    /**
     * @return If true, register the handler with the client.
     */
    boolean syncToClient() default true;
}
