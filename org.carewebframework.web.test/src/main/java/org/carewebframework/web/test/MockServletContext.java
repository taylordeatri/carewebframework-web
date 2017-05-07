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
package org.carewebframework.web.test;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Mock servlet context for unit testing.
 */
public class MockServletContext extends org.springframework.mock.web.MockServletContext {
    
    public static class ResourceLoader extends DefaultResourceLoader {
        
        @Override
        public Resource getResource(String location) {
            if (location != null && location.startsWith("/web") || location.startsWith("web")) {
                location = "classpath:" + location;
            }

            return super.getResource(location);
        }

    }

    public MockServletContext() {
        super(new ResourceLoader());
        this.setAttribute("javax.websocket.server.ServerContainer", new MockServerContainer());
    }
    
    @Override
    public String getRealPath(String path) {
        try {
            URL url = getResource(path);
            
            if (url == null) {
                return null;
            }

            String protocol = url.getProtocol();

            if ("jar".equals(protocol)) {
                return url.toString();
            }
        } catch (MalformedURLException e) {
            // NOP
        }
        
        return super.getRealPath(path);
    }

}
