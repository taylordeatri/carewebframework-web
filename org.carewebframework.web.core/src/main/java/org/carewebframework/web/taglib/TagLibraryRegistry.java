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
package org.carewebframework.web.taglib;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.MiscUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

public class TagLibraryRegistry extends AbstractRegistry<String, TagLibrary> implements ApplicationContextAware {
    
    private static Log log = LogFactory.getLog(TagLibraryRegistry.class);
    
    private static TagLibraryRegistry instance = new TagLibraryRegistry();
    
    public static TagLibraryRegistry getInstance() {
        return instance;
    }
    
    private TagLibraryRegistry() {
    }
    
    @Override
    protected String getKey(TagLibrary item) {
        return item.getUri();
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            Resource[] resources = applicationContext.getResources("classpath*:**/*.tld");
            
            for (Resource resource : resources) {
                log.info("Found tag library at " + resource);
                
                try {
                    register(TagLibraryParser.getInstance().parse(resource));
                } catch (Exception e) {
                    log.error(e);
                }
            }
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
