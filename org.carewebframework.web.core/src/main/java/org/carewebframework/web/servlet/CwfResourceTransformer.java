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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.client.WebJarLocator;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.core.RequestUtil;
import org.carewebframework.web.core.WebUtil;
import org.carewebframework.web.logging.LogUtil;
import org.carewebframework.web.theme.Theme;
import org.carewebframework.web.theme.ThemeRegistry;
import org.carewebframework.web.theme.ThemeResolver;
import org.springframework.core.io.AbstractFileResolvingResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.EncodedResource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.ResourceTransformerSupport;

public class CwfResourceTransformer extends ResourceTransformerSupport {

    private static class BootstrapperResource extends AbstractFileResolvingResource implements EncodedResource {

        private final Resource resource;

        private final StringBuffer content = new StringBuffer();

        BootstrapperResource(Resource resource) {
            this.resource = resource;
        }

        public void addContent(String data) {
            content.append(data).append('\n');
        }

        @Override
        public long contentLength() throws IOException {
            return content.length();
        }

        @Override
        public String getFilename() {
            return resource.getFilename();
        }

        @Override
        public URL getURL() throws IOException {
            return resource.getURL();
        }

        @Override
        public String getDescription() {
            return resource.getDescription();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return IOUtils.toInputStream(content.toString(), StandardCharsets.UTF_8);
        }

        @Override
        public String getContentEncoding() {
            return "html";
        }

    }

    private final List<String> bootstrapperTemplate;

    private final ThemeResolver themeResolver = new ThemeResolver();

    public CwfResourceTransformer() {
        try {
            bootstrapperTemplate = IOUtils.readLines(getClass().getResourceAsStream("/web/cwf/bootstrapper.htm"),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }

    /**
     * If the resource is a CWF resource (i.e., has file extension of ".cwf"), replace it with an
     * html resource derived from the bootstrapping template.
     */
    @Override
    public Resource transform(HttpServletRequest request, Resource resource,
                              ResourceTransformerChain chain) throws IOException {

        if (resource == null || !resource.getFilename().endsWith(".cwf")) {
            return chain.transform(request, resource);
        }

        request.getSession(true);
        BootstrapperResource bootstrapperResource = new BootstrapperResource(resource);
        Map<String, String> map = new HashMap<>();
        Page page = Page._create(resource.getURL().toString());
        String baseUrl = RequestUtil.getBaseURL(request);
        String wsUrl = "ws" + baseUrl.substring(4) + "ws";
        String themeName = themeResolver.resolveThemeName(request);
        Theme theme = themeName == null ? null : ThemeRegistry.getInstance().get(themeName);
        String webJarInit = theme == null ? WebJarLocator.getInstance().getWebJarInit() : theme.getWebJarInit();
        map.put("pid", page.getId());
        map.put("baseUrl", baseUrl);
        map.put("wsUrl", wsUrl);
        map.put("webjarInit", webJarInit);
        map.put("debug", Boolean.toString(WebUtil.isDebugEnabled()));
        map.put("logging", LogUtil.getSettingsForClient());
        StrSubstitutor sub = new StrSubstitutor(map);

        for (String line : bootstrapperTemplate) {
            bootstrapperResource.addContent(sub.replace(line));
        }

        return chain.transform(request, bootstrapperResource);
    }

}
