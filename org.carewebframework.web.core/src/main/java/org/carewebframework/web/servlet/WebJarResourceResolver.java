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
package org.carewebframework.web.servlet;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.carewebframework.web.client.WebJar;
import org.carewebframework.web.client.WebJarLocator;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.AbstractResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

/**
 * Inserts web jar version into request path. Converts
 * <p>
 * <code>webjars/{package-name}/**</code>
 * </p>
 * to
 * <p>
 * <code>webjars/{package-name}/{package-version}/**</code>
 * </p>
 */
public class WebJarResourceResolver extends AbstractResourceResolver {
    
    private String getResourcePath(String path) {
        int i = path.indexOf("/");
        String module = path.substring(0, i);
        WebJar webjar = WebJarLocator.getInstance().getWebjar(module);
        return webjar == null ? path : path.substring(0, i) + "/" + webjar.getVersion() + path.substring(i);
    }

    @Override
    protected Resource resolveResourceInternal(HttpServletRequest request, String requestPath,
                                               List<? extends Resource> locations, ResourceResolverChain chain) {
        requestPath = getResourcePath(requestPath);
        return chain.resolveResource(request, requestPath, locations);
    }
    
    @Override
    protected String resolveUrlPathInternal(String resourceUrlPath, List<? extends Resource> locations,
                                            ResourceResolverChain chain) {
        
        resourceUrlPath = getResourcePath(resourceUrlPath);
        return chain.resolveUrlPath(resourceUrlPath, locations);
    }
}
