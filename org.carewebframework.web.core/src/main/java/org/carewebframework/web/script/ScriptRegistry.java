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
package org.carewebframework.web.script;

import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.RegistryMap.DuplicateAction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Tracks script extensions.
 */
public class ScriptRegistry extends AbstractRegistry<String, IScriptLanguage> implements BeanPostProcessor {
    
    private static final ScriptRegistry instance = new ScriptRegistry();
    
    public static ScriptRegistry getInstance() {
        return instance;
    }
    
    private ScriptRegistry() {
        super(DuplicateAction.ERROR);
    }
    
    @Override
    protected String getKey(IScriptLanguage script) {
        return script.getType();
    }
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IScriptLanguage) {
            register((IScriptLanguage) bean);
        }
        
        return bean;
    }
    
}
