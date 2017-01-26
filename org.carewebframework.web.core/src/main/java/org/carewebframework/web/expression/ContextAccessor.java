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
package org.carewebframework.web.expression;

import org.carewebframework.web.component.BaseComponent;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

/**
 * Property accessor for resolving property references during materialization.
 */
public class ContextAccessor implements PropertyAccessor {
    
    private static final Class<?>[] TARGET_CLASSES = { ELContext.class, BaseComponent.class };
    
    @Override
    public Class<?>[] getSpecificTargetClasses() {
        return TARGET_CLASSES;
    }
    
    @Override
    public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
        return !(target instanceof BaseComponent) || ((BaseComponent) target).hasAttribute(name);
    }
    
    @Override
    public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
        Object result = null;
        
        if (target instanceof ELContext) {
            result = ((ELContext) target).getValue(name);
        } else if (target instanceof BaseComponent) {
            result = ((BaseComponent) target).getAttribute(name);
        }
        
        return new TypedValue(result);
    }
    
    @Override
    public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
        return false;
    }
    
    @Override
    public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        throw new AccessException("Property source is read-only.");
    }
    
}
