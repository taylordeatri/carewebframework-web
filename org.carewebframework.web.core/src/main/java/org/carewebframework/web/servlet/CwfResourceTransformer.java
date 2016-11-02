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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.client.ClientUtil;
import org.carewebframework.web.client.WebJarLocator;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.logging.LogUtil;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.EncodedResource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.ResourceTransformerSupport;

public class CwfResourceTransformer extends ResourceTransformerSupport {
    
    private static class BootstrapperResource extends AbstractResource implements EncodedResource {
        
        private final Resource resource;
        
        private final StringBuffer content = new StringBuffer();
        
        BootstrapperResource(Resource resource) {
            this.resource = resource;
        }
        
        public void addContent(String data) {
            content.append(data).append('\n');
        }
        
        @Override
        public File getFile() throws IOException {
            return resource.getFile();
        }
        
        @Override
        public String getFilename() {
            return resource.getFilename() + ".htm";
        }
        
        @Override
        public long contentLength() throws IOException {
            return content.length();
        }
        
        @Override
        public String getDescription() {
            return resource.getDescription();
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            return IOUtils.toInputStream(content.toString());
        }
        
        @Override
        public String getContentEncoding() {
            return "html";
        }
        
    }
    
    private final List<String> bootstrapperTemplate;
    
    public CwfResourceTransformer() {
        try {
            bootstrapperTemplate = IOUtils.readLines(getClass().getResourceAsStream("/web/cwf/bootstrapper.htm"));
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
        String path = (request.isSecure() ? "s" : "") + "://" + request.getServerName() + ":" + request.getServerPort()
                + request.getContextPath() + "/";
        String wsUrl = "ws" + path + "ws";
        String rootUrl = "http" + path;
        map.put("pid", page.getId());
        map.put("rootUrl", rootUrl);
        map.put("wsUrl", wsUrl);
        map.put("webjarInit", WebJarLocator.getInstance().getWebJarInit());
        map.put("debug", Boolean.toString(ClientUtil.debugEnabled()));
        map.put("logging", LogUtil.getSettingsForClient());
        StrSubstitutor sub = new StrSubstitutor(map);
        
        for (String line : bootstrapperTemplate) {
            bootstrapperResource.addContent(sub.replace(line));
        }
        
        return chain.transform(request, bootstrapperResource);
    }
    
}