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
package org.carewebframework.web.spring;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.expression.ParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * Provides namespace support for scanning for CWF component annotations. For example,
 *
 * <pre>
 * {@code
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *    xmlns:cwc="http://www.carewebframework.org/schema/component"
 *    xsi:schemaLocation="
 *        http://www.springframework.org/schema/beans
 *        http://www.springframework.org/schema/beans/spring-beans.xsd
 *        http://www.carewebframework.org/schema/component
 *        http://www.carewebframework.org/schema/component/component-extensions.xsd">
 *
 *    <!-- Scan by package -->
 *    <cwc:component-scan package="org.carewebframework.shell" />
 *    <!-- Scan by class -->
 *    <cwc:component-scan class="org.carewebframework.shell.CareWebShell" />
 * </beans>
 * }
 * </pre>
 */
public class ComponentXmlParser extends AbstractSingleBeanDefinitionParser {

    private static final String[] ATTRIBUTES = { "class", "package" };

    @Override
    protected String getBeanClassName(Element element) {
        return "org.springframework.beans.factory.config.MethodInvokingBean";
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        if (element.getAttributes().getLength() > 1) {
            error("Invalid attributes specified");
        }
        
        Attr attr = (Attr) element.getAttributes().item(0);
        int i = attr == null ? -2 : ArrayUtils.indexOf(ATTRIBUTES, attr.getName());
        
        switch (i) {
            case -2: // Missing attribute
                error("Missing attribute");
                
            case -1: // Unknown attribute
                error("Unrecognized attribute: " + attr.getName());
                
            case 0: // class
                builder.addPropertyValue("targetMethod", "scanClass");
                break;

            case 1: // package
                builder.addPropertyValue("targetMethod", "scanPackage");
                break;

        }

        builder.addPropertyReference("targetObject", "cwf_ComponentScanner");
        builder.addPropertyValue("arguments", new Object[] { attr.getValue() });
    }
    
    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    private void error(String message) {
        throw new ParseException(0, message + ".\n" + "Must be one of: " + ATTRIBUTES);
    }
}
