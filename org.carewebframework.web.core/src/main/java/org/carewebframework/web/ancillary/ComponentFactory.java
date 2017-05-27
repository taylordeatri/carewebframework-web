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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.annotation.Component.FactoryParameter;
import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.expression.ELEvaluator;

/**
 * Factory to be used during component creation. Factory parameters may be modified during
 * deserialization to provide control over component creation.
 */
public class ComponentFactory {
    
    private final ComponentDefinition def;
    
    private Class<? extends BaseComponent> clazz;
    
    private boolean inactive;
    
    public ComponentFactory(ComponentDefinition def) {
        this.def = def;
        this.clazz = def.getComponentClass();
    }
    
    /**
     * A special processor may modify the component's implementation class, as long as the
     * substituted class is a subclass of the original.
     *
     * @param clazz Component implementation class to substitute.
     */
    @FactoryParameter("impl")
    public void setImplementationClass(Class<? extends BaseComponent> clazz) {
        Class<? extends BaseComponent> originalClazz = def.getComponentClass();

        if (clazz != null && !originalClazz.isAssignableFrom(clazz)) {
            throw new ComponentException("Implementation class must extend class " + originalClazz.getName());
        }
        
        this.clazz = clazz;
    }
    
    /**
     * Conditionally prevents the factory from creating a component.
     *
     * @param condition If false, prevent factory from creating a component.
     */
    @FactoryParameter("if")
    protected void setIf(boolean condition) {
        inactive = !condition;
    }
    
    /**
     * Conditionally prevents the factory from creating a component.
     *
     * @param condition If true, prevent factory from creating a component.
     */
    @FactoryParameter("unless")
    protected void setUnless(boolean condition) {
        inactive = condition;
    }
    
    /**
     * Returns true if component creation has been inactivated.
     *
     * @return True prevents component creation.
     */
    public boolean isInactive() {
        return inactive;
    }

    /**
     * Creates a component instance from the definition using a factory context.
     *
     * @param attributes Attribute map for initializing.
     * @return A component instance. May be null if creation is suppressed.
     */
    public BaseComponent create(Map<String, String> attributes) {
        if (attributes != null) {
            for (Entry<String, Method> entry : def.getFactoryParameters().entrySet()) {
                String name = entry.getKey();
                
                if (attributes.containsKey(name)) {
                    Object value = ELEvaluator.getInstance().evaluate(attributes.remove(name));
                    ConvertUtil.invokeSetter(this, entry.getValue(), value);
                }
            }
        }
        
        try {
            return inactive ? null : clazz.newInstance();
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
