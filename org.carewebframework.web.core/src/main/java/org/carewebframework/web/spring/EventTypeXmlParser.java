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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * Provides namespace support for scanning for CWF event type annotations. For example,
 *
 * <pre>
 * {@code
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *    xmlns:cwe="http://www.carewebframework.org/schema/eventtype"
 *    xsi:schemaLocation="
 *        http://www.springframework.org/schema/beans
 *        http://www.springframework.org/schema/beans/spring-beans.xsd
 *        http://www.carewebframework.org/schema/eventtype
 *        http://www.carewebframework.org/schema/eventtype/eventtype-extensions.xsd">
 *
 *    <!-- Scan by package -->
 *    <cwe:eventtype-scan package="org.carewebframework.web.event" />
 *    <!-- Scan by class -->
 *    <cwe:eventtype-scan class="org.carewebframework.web.event.ChangeEvent" />
 * </beans>
 * }
 * </pre>
 */
public class EventTypeXmlParser extends AbstractXmlParser {
    
    @Override
    protected void setTargetObject(BeanDefinitionBuilder builder) {
        builder.addPropertyReference("targetObject", "cwf_EventTypeScanner");
    }
}
