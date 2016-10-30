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

import org.carewebframework.web.annotation.ComponentScanner;
import org.carewebframework.web.annotation.EventTypeScanner;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.event.Event;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.AppCacheManifestTransformer;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@EnableWebMvc
@Configuration
public class ServletConfiguration extends WebMvcConfigurerAdapter {
    
    private final GzipResourceResolver gzipResourceResolver = new GzipResourceResolver();
    
    private final VersionResourceResolver versionResourceResolver = new VersionResourceResolver()
            .addContentVersionStrategy("/**");
    
    private final CwfResourceTransformer cwfResourceTransformer = new CwfResourceTransformer();
    
    private final AppCacheManifestTransformer appCacheManifestTransformer = new AppCacheManifestTransformer();
    
    private final MinifiedResourceResolver minifiedResourceResolver = new MinifiedResourceResolver("js", "css");
    
    public ServletConfiguration() throws Exception {
        ComponentScanner.getInstance().scan(BaseComponent.class.getPackage());
        EventTypeScanner.getInstance().scan(Event.class.getPackage());
    }
    
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("cwf", MediaType.TEXT_HTML);
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        addResourceHandlers(registry, "/webjars/**", "classpath:/META-INF/resources/webjars/");
        addResourceHandlers(registry, "/web/**", "classpath:/web/");
        addResourceHandlers(registry, "/**", "/");
    }
    
    private void addResourceHandlers(ResourceHandlerRegistry registry, String pattern, String locations) {
        //@formatter:off
        registry
            .addResourceHandler(pattern)
            .addResourceLocations(locations)
            .resourceChain(false)
            .addResolver(versionResourceResolver)
            .addResolver(minifiedResourceResolver)
            .addResolver(gzipResourceResolver)
            .addTransformer(cwfResourceTransformer)
            .addTransformer(appCacheManifestTransformer);
        //@formatter:on
    }
    
}
