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
package org.carewebframework.web.theme;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.client.WebJarLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads themes from all theme-*.properties files.
 */
public class ThemeRegistry extends AbstractRegistry<String, Theme> implements ApplicationContextAware {

    private static final Log log = LogFactory.getLog(ThemeRegistry.class);

    private static final ThemeRegistry instance = new ThemeRegistry();
    
    public static ThemeRegistry getInstance() {
        return instance;
    }
    
    @Override
    protected String getKey(Theme theme) {
        return theme.getName();
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ObjectMapper parser = new ObjectMapper().configure(ALLOW_UNQUOTED_FIELD_NAMES, true).configure(ALLOW_SINGLE_QUOTES,
            true);
        loadThemes(applicationContext, parser, "classpath*:META-INF");
        loadThemes(applicationContext, parser, "WEB-INF");
    }
    
    private void loadThemes(ApplicationContext applicationContext, ObjectMapper parser, String path) {
        try {
            for (Resource resource : applicationContext.getResources(path + "/theme.properties")) {
                try (InputStream in = resource.getInputStream()) {
                    Properties props = new Properties();
                    props.load(in);

                    for (Entry<Object, Object> entry : props.entrySet()) {
                        String key = entry.getKey().toString();
                        String value = entry.getValue().toString();
                        int i = key.indexOf("/");
                        String themeName = key.substring(0, i);
                        key = key.substring(i + 1);
                        Theme theme = get(themeName);
                        
                        if (theme == null) {
                            theme = new Theme(themeName, WebJarLocator.getInstance().getConfig());
                            register(theme);
                            log.info("Registered theme: " + themeName);
                        }
                        
                        theme.addPath(key, value);
                    }
                } catch (Exception e) {
                    log.error("Error reading theme configuration data from " + resource, e);
                }

            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
}
