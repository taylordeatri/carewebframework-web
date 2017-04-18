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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.AbstractRegistry;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.client.WebJarLocator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

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
        loadThemes(applicationContext, "classpath*:META-INF");
        loadThemes(applicationContext, "WEB-INF");
    }
    
    private void loadThemes(ApplicationContext applicationContext, String path) {
        try {
            for (Resource resource : applicationContext.getResources(path + "/theme.properties")) {
                try (InputStream in = resource.getInputStream()) {
                    Properties props = new Properties();
                    props.load(in);

                    for (Entry<Object, Object> entry : props.entrySet()) {
                        String key = entry.getKey().toString();
                        String value = entry.getValue().toString();
                        updateTheme(key, value);
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
    
    /**
     * Fetches the named theme from the registry. If one does not exist, it is created and
     * registered.
     *
     * @param themeName The theme name.
     * @return The corresponding theme.
     */
    private Theme getOrCreateTheme(String themeName) {
        Theme theme = get(themeName);
        
        if (theme == null) {
            theme = new Theme(themeName, WebJarLocator.getInstance().getConfig());
            register(theme);
            log.info("Registered theme: " + themeName);
        }
        
        return theme;
    }
    
    /**
     * Updates a theme's configuration.
     *
     * @param configPath The "/"-delimited path to the configuration node to be updated. The first
     *            piece of the path is the name of the theme to be modified.
     * @param value The new value. If null or empty, the configuration node will be removed.
     */
    private void updateTheme(String configPath, String value) {
        String[] path = configPath.split("\\/");
        Theme theme = getOrCreateTheme(path[0]);
        ObjectNode config = theme.getConfig();
        ObjectNode node = config;
        int last = path.length - 1;
        value = StringUtils.trimToNull(value);

        for (int i = 1; i <= last; i++) {
            String pnode = path[i];
            ObjectNode parent = node;

            if (i == last) {
                if (value == null) {
                    parent.remove(pnode);
                } else {
                    parent.set(pnode, new TextNode(value));
                }

                break;
            }

            node = (ObjectNode) parent.get(pnode);

            if (node == null) {
                if (value == null) {
                    return;
                } else {
                    node = config.objectNode();
                    parent.set(pnode, node);
                }
            }
        }
    }
    
}
