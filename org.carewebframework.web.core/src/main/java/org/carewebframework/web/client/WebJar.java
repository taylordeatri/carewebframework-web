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
package org.carewebframework.web.client;

import java.io.IOException;

import org.carewebframework.common.MiscUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Information describing a single web jar resource.
 */
public class WebJar {
    
    private final Resource resource;
    
    private final String module;

    private final String version;

    private final String absolutePath;
    
    public WebJar(Resource resource) {
        try {
            this.resource = resource;
            absolutePath = resource.getURL().toString();
            int i = absolutePath.lastIndexOf("/webjars/") + 9;
            int j = absolutePath.indexOf("/", i);
            module = absolutePath.substring(i, j);
            i = absolutePath.indexOf("/", j + 1);
            version = absolutePath.substring(j + 1, i);
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
    
    public String getRootPath() {
        return "webjars/" + module + "/";
    }

    public String getModule() {
        return module;
    }

    public String getVersion() {
        return version;
    }
    
    public Resource createRelative(String relativePath) {
        try {
            return resource.createRelative(relativePath);
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    /**
     * Finds the first web jar resource that matches one of the specified file extensions.
     *
     * @param resourceLoader The resource loader that will perform the search.
     * @param extensions The file extensions to match.
     * @return The first matching resource encountered, or null if none found.
     */
    public Resource findResource(ResourcePatternResolver resourceLoader, String... extensions) {
        try {
            String path = getRootPath();

            for (String extension : extensions) {
                Resource[] resources = resourceLoader.getResources(path + "**/*." + extension);

                if (resources.length > 0) {
                    return resources[0];
                }
            }
        } catch (Exception e) {}

        return null;
    }
    
    @Override
    public String toString() {
        return "webjar:" + module + ":" + version;
    }
}
