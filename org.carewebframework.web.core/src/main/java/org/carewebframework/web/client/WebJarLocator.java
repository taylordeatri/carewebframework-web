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

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.common.MiscUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Locates all web jars on the class path, parses their configuration data (supports classic, NPM,
 * and Bower formats), and generates the necessary initialization code for RequireJS.
 */
public class WebJarLocator implements ApplicationContextAware {
    
    private static final Log log = LogFactory.getLog(WebJarLocator.class);
    
    private static final WebJarLocator instance = new WebJarLocator();
    
    private String webjarInit;
    
    private ApplicationContext applicationContext;
    
    public static WebJarLocator getInstance() {
        return instance;
    }
    
    private WebJarLocator() {
    }
    
    public String getWebJarInit() {
        return webjarInit;
    }
    
    /**
     * Locate and process all web jars.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        
        try {
            Resource[] resources = applicationContext.getResources("classpath*:/META-INF/resources/webjars/?*/?*/");
            ObjectMapper parser = new ObjectMapper().configure(ALLOW_UNQUOTED_FIELD_NAMES, true)
                    .configure(ALLOW_SINGLE_QUOTES, true);
            ObjectNode requireConfig = parser.createObjectNode();
            requireConfig.set("paths", parser.createObjectNode());
            requireConfig.set("packages", parser.createArrayNode());
            
            for (Resource resource : resources) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Parsing configuration data for web jar " + resource.getFilename());
                    }
                    boolean success = tryRequireFormat(resource, requireConfig, parser)
                            || tryBowerFormat(resource, requireConfig, parser)
                            || tryNPMFormat(resource, requireConfig, parser) || tryNoFormat(resource, requireConfig);
                    
                    if (!success) {
                        throw new Exception("Unrecognized webjar package format.");
                    }
                } catch (Exception e) {
                    log.error("Error extracting webjar configuration from " + resource, e);
                }
            }
            
            webjarInit = requireConfig.toString();
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    /**
     * Returns the root path for web jar resources.
     * 
     * @param resource The resource for the folder containing web jar resources.
     * @return The root path.
     * @throws IOException Should never occur.
     */
    private String getRootPath(Resource resource) throws IOException {
        String path = resource.getURL().toString();
        int i = path.lastIndexOf("/webjars/") + 1;
        return path.substring(i);
    }
    
    /**
     * Determine if packaged as RequireJS and process if so. To do this, we have to locate the
     * pom.xml resource and search it for the "requirejs" property entry. If this is found, it is
     * parsed and merged with the RequireJS configuration that we are building.
     * 
     * @param resource The folder containing web jar resources.
     * @param requireConfig The RequireJS configuration we are building.
     * @param parser The JSON parser.
     * @return True if successfully processed.
     */
    private boolean tryRequireFormat(Resource resource, ObjectNode requireConfig, ObjectMapper parser) {
        try {
            String pomPath = resource.getURL().toString();
            int i = pomPath.lastIndexOf("/META-INF/") + 10;
            pomPath = pomPath.substring(0, i) + "maven/**/pom.xml";
            Resource[] poms = applicationContext.getResources(pomPath);
            JsonNode config = poms.length == 0 ? null : extractConfig(poms[0], parser);
            
            if (config != null) {
                String rootPath = getRootPath(resource);
                addVersionToPath(config, rootPath);
                addVersionToPackage(config, rootPath);
                merge(requireConfig, config);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Merges one JSON tree (srcNode) into another (destNode).
     * 
     * @param destNode The tree receiving the merged node.
     * @param srcNode The tree supplying the nodes to merge.
     */
    private void merge(JsonNode destNode, JsonNode srcNode) {
        Iterator<String> fieldNames = srcNode.fieldNames();
        
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode jsonNode = destNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, srcNode.get(fieldName));
            } else if (destNode instanceof ObjectNode) {
                // Overwrite field
                JsonNode value = srcNode.get(fieldName);
                ((ObjectNode) destNode).set(fieldName, value);
            }
        }
    }
    
    /**
     * Add root path to the path entries of the parsed RequireJS config.
     * 
     * @param configNode Top level node of the parsed RequireJS config.
     * @param path The root path.
     */
    private void addVersionToPath(JsonNode configNode, String path) {
        ObjectNode paths = (ObjectNode) configNode.get("paths");
        
        if (paths != null) {
            Iterator<Entry<String, JsonNode>> iter = paths.fields();
            
            while (iter.hasNext()) {
                Entry<String, JsonNode> entry = iter.next();
                JsonNode child = entry.getValue();
                
                if (child.isTextual()) {
                    ArrayNode newChild = paths.arrayNode();
                    newChild.add(path + child.asText());
                    entry.setValue(newChild);
                }
            }
        }
    }
    
    /**
     * Add root path to the package entries of the parsed RequireJS config.
     * 
     * @param configNode Top level node of the parsed RequireJS config.
     * @param path The root path.
     */
    private void addVersionToPackage(JsonNode configNode, String path) {
        ArrayNode packages = (ArrayNode) configNode.get("packages");
        
        if (packages != null) {
            for (int i = 0; i < packages.size(); i++) {
                ObjectNode entry = (ObjectNode) packages.get(i);
                JsonNode location = entry.get("location");
                
                if (location != null) {
                    String value = location.asText();
                    entry.set("location", new TextNode(path + value));
                }
            }
        }
        
    }
    
    /**
     * Extracts and parses the "requirejs" property value from the pom.xml.
     * 
     * @param pomResource The pom.xml resource.
     * @param parser The JSON parser.
     * @return The parsed value, or null if the "requirejs" property was not found.
     * @throws Exception Unspecified exception.
     */
    private JsonNode extractConfig(Resource pomResource, ObjectMapper parser) throws Exception {
        try (InputStream is = pomResource.getInputStream();) {
            Iterator<String> iter = IOUtils.lineIterator(is, "UTF-8");
            StringBuilder sb = new StringBuilder();
            boolean found = false;
            
            while (iter.hasNext()) {
                String line = iter.next();
                int i;
                
                if (!found) {
                    i = line.indexOf("<requirejs>");
                    
                    if (i == -1) {
                        continue;
                    }
                    
                    line = line.substring(i + 11);
                    found = true;
                }
                
                i = line.indexOf("</requirejs>");
                
                if (i >= 0) {
                    sb.append(line.substring(0, i));
                    break;
                }
                
                sb.append(line);
            }
            
            String requirejs = sb.toString().trim();
            return requirejs.isEmpty() ? null : parser.readTree(requirejs);
        }
    }
    
    /**
     * Determine if packaged as Bower and process if so.
     * 
     * @param resource The folder containing web jar resources.
     * @param requireConfig The RequireJS configuration we are building.
     * @param parser The JSON parser.
     * @return True if successfully processed.
     */
    private boolean tryBowerFormat(Resource resource, ObjectNode requireConfig, ObjectMapper parser) {
        return tryBowerOrNPMFormat("bower.json", resource, requireConfig, parser);
    }
    
    /**
     * Determine if packaged as NPM and process if so.
     * 
     * @param resource The folder containing web jar resources.
     * @param requireConfig The RequireJS configuration we are building.
     * @param parser The JSON parser.
     * @return True if successfully processed.
     */
    private boolean tryNPMFormat(Resource resource, ObjectNode requireConfig, ObjectMapper parser) {
        return tryBowerOrNPMFormat("package.json", resource, requireConfig, parser);
    }
    
    private boolean tryBowerOrNPMFormat(String configFile, Resource resource, ObjectNode requireConfig,
                                        ObjectMapper parser) {
        
        try {
            Resource configResource = resource.createRelative(configFile);
            
            if (configResource.exists()) {
                try (InputStream is = configResource.getInputStream();) {
                    JsonNode config = parser.readTree(is);
                    ObjectNode paths = (ObjectNode) requireConfig.get("paths");
                    ArrayNode entries = paths.arrayNode();
                    String name = config.get("name").asText();
                    String path = getRootPath(resource);
                    
                    if (!addMain(config.get("main"), entries, path)) {
                        return false;
                    }
                    
                    paths.set(name, entries);
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        
        return false;
    }
    
    /**
     * Adds the "main" entry to the RequireJS config we are building.
     * 
     * @param node The node corresponding to the "main" or "files" entry. If this is an array, we
     *            recurse over each element.
     * @param entries The node to receive the "paths" entries.
     * @param path The root path.
     * @return True if entries added.
     */
    private boolean addMain(JsonNode node, ArrayNode entries, String path) {
        boolean added = false;
        
        if (node != null) {
            if (node.isArray()) {
                Iterator<JsonNode> iter = node.elements();
                
                while (iter.hasNext()) {
                    added |= addMain(iter.next(), entries, path);
                }
            } else {
                String main = node.asText();
                
                if (main.endsWith(".js")) {
                    int i = main.lastIndexOf(".");
                    main = main.substring(0, i);
                    entries.add(path + main);
                    added = true;
                }
            }
        }
        
        return added;
    }
    
    /**
     * In absence of a supported format, try to infer the RequireJS config.
     * 
     * @param resource The folder containing web jar resources.
     * @param requireConfig The RequireJS configuration we are building.
     * @return True if successfully processed.
     */
    private boolean tryNoFormat(Resource resource, ObjectNode requireConfig) throws Exception {
        String path = getRootPath(resource);
        String main = findResources(path, "js");
        main = main == null ? findResources(path, "css") : main;
        
        if (main == null) {
            return false;
        }
        
        log.warn("Unknown web jar packaging, so inferring configuration for " + resource);
        int i = main.lastIndexOf(".");
        main = main.substring(0, i);
        i = main.indexOf("webjars/") + 8;
        int j = main.indexOf("/", i);
        String name = main.substring(i, j);
        ObjectNode paths = (ObjectNode) requireConfig.get("paths");
        ArrayNode entries = paths.arrayNode();
        entries.add(main);
        entries.add(main.substring(path.length()));
        paths.set(name, entries);
        return true;
    }
    
    /**
     * Find all resources with the specified extension under the given path.
     * 
     * @param path The path under which to search.
     * @param ext The file extension to search for.
     * @return The first matching resource encountered, or null if none found.
     */
    private String findResources(String path, String ext) throws Exception {
        String main = null;
        String minExt = ".min." + ext;
        
        for (Resource jsResource : applicationContext.getResources(path + "**/*." + ext)) {
            main = getRootPath(jsResource);
            
            if (!main.endsWith(minExt)) {
                break;
            }
        }
        
        return main;
    }
}
