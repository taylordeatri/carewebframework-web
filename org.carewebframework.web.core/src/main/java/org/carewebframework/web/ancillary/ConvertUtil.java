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

import org.apache.commons.beanutils.ConvertUtils;
import org.carewebframework.common.StrUtil;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;

public class ConvertUtil {
    
    /**
     * Converts an input value to a target type.
     * 
     * @param value The value to convert.
     * @param targetType The type to which to convert.
     * @param instance The object instance whose property value is to be set (necessary when the
     *            target type is a component and the value is the component name or id).
     * @return The converted value.
     */
    public static Object convert(Object value, Class<?> targetType, Object instance) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        
        if (targetType.isEnum()) {
            return convertToEnum(value, targetType);
        }
        
        if (BaseComponent.class.isAssignableFrom(targetType)) {
            return convertToComponent(value, targetType, instance);
        }
        
        return ConvertUtils.convert(value, targetType);
    }
    
    /**
     * Converts the input value to an enumeration member. The input value must resolve to a string
     * which is then matched to an enumeration member by using a case-insensitive lookup.
     * 
     * @param value The value to convert.
     * @param enumType The enumeration type.
     * @return The enumeration member corresponding to the input value.
     */
    private static Object convertToEnum(Object value, Class<?> enumType) {
        String val = (String) convert(value, String.class, null);
        
        for (Object e : enumType.getEnumConstants()) {
            if (((Enum<?>) e).name().equalsIgnoreCase(val)) {
                return e;
            }
        }
        
        throw new IllegalArgumentException(
                StrUtil.formatMessage("The value \"%s\" is not a member of the enumeration %s", value, enumType.getName()));
    }
    
    /**
     * Converts the input value to component. The input value must resolve to a string which
     * represents the name or id of the component sought. This name is resolved to a component
     * instance by looking it up in the namespace of the provided component instance.
     * 
     * @param value The value to convert.
     * @param componentType The component type.
     * @param instance The component whose namespace will be used for lookup.
     * @return The component whose name matches the input value.
     */
    private static BaseComponent convertToComponent(Object value, Class<?> componentType, Object instance) {
        if (!BaseComponent.class.isInstance(instance)) {
            StrUtil.formatMessage("The property owner is not of the expected type (was %s but expected %s)",
                instance.getClass().getName(), BaseComponent.class.getName());
        }
        
        String name = (String) convert(value, String.class, instance);
        BaseComponent container = (BaseComponent) instance;
        BaseComponent target = name.startsWith(Page.ID_PREFIX) ? container.getPage().findById(name)
                : container.findByName(name);
        
        if (target == null) {
            throw new IllegalArgumentException(
                    StrUtil.formatMessage("A component with name or id \"%s\" was not found", name));
        }
        
        if (!componentType.isInstance(target)) {
            throw new IllegalArgumentException(StrUtil.formatMessage(
                "The component with name or id \"%s\" is not of the expected type (was %s but expected %s)", name,
                target.getClass().getName(), componentType.getName()));
        }
        
        return target;
    }
    
    private ConvertUtil() {
    }
}