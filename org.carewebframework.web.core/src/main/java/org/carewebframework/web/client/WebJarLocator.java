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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

    private final Map<String, WebJar> webjars = new HashMap<>();

    public static WebJarLocator getInstance() {
        return instance;
    }

    private WebJarLocator() {
    }

    public String getWebJarInit() {
        return webjarInit;
    }

    public WebJar getWebjar(String module) {
        return webjars.get(module);
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

                    WebJar webjar = new WebJar(resource);
                    boolean success = tryRequireFormat(webjar, requireConfig, parser)
                            || tryBowerFormat(webjar, requireConfig, parser) || tryNPMFormat(webjar, requireConfig, parser)
                            || tryUnknownFormat(webjar, requireConfig);

                    if (!success) {
                        throw new Exception("Unrecognized webjar package format.");
                    }

                    webjars.put(webjar.getModule(), webjar);
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
     * Determine if packaged as RequireJS and process if so. To do this, we have to locate the
     * pom.xml resource and search it for the "requirejs" property entry. If this is found, it is
     * parsed and merged with the RequireJS configuration that we are building.
     *
     * @param webjar The webjar.
     * @param requireConfig The RequireJS configuration we are building.
     * @param parser The JSON parser.
     * @return True if successfully processed.
     */
    private boolean tryRequireFormat(WebJar webjar, ObjectNode requireConfig, ObjectMapper parser) {
        try {
            String pomPath = webjar.getAbsolutePath();
            int i = pomPath.lastIndexOf("/META-INF/") + 10;
            pomPath = pomPath.substring(0, i) + "maven/**/pom.xml";
            Resource[] poms = applicationContext.getResources(pomPath);
            JsonNode config = poms.length == 0 ? null : extractConfig(poms[0], parser);

            if (config != null) {
                String rootPath = webjar.getRootPath();
                addPathToPaths(config, rootPath);
                addPathToPackages(config, rootPath, parser);
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
    private void addPathToPaths(JsonNode configNode, String path) {
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
     * @param parser The JSON parser.
     */
    private void addPathToPackages(JsonNode configNode, String path, ObjectMapper parser) {
        ArrayNode packages = (ArrayNode) configNode.get("packages");

        if (packages != null) {
            for (int i = 0; i < packages.size(); i++) {
                String nameValue, locationValue;
                JsonNode entry = packages.get(i);
                ObjectNode object;

                if (entry.isTextual()) {
                    nameValue = entry.asText();
                    locationValue = nameValue;
                    object = parser.createObjectNode();
                    packages.set(i, object);
                    object.set("main", new TextNode("main"));
                    object.set("name", new TextNode(nameValue));
                } else {
                    object = (ObjectNode) entry;
                    JsonNode location = object.get("location");
                    JsonNode name = object.get("name");
                    nameValue = name == null ? "" : name.asText();
                    locationValue = location == null ? nameValue : "./";//location.asText();
                }

                object.set("location", new TextNode(path + locationValue));
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
            Iterator<String> iter = IOUtils.lineIterator(is, StandardCharsets.UTF_8);
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
     * @param webjar The web jar.
     * @param requireConfig The RequireJS configuration we are building.
     * @param parser The JSON parser.
     * @return True if successfully processed.
     */
    private boolean tryBowerFormat(WebJar webjar, ObjectNode requireConfig, ObjectMapper parser) {
        return tryBowerOrNPMFormat("bower.json", webjar, requireConfig, parser);
    }

    /**
     * Determine if packaged as NPM and process if so.
     *
     * @param webjar The web jar.
     * @param requireConfig The RequireJS configuration we are building.
     * @param parser The JSON parser.
     * @return True if successfully processed.
     */
    private boolean tryNPMFormat(WebJar webjar, ObjectNode requireConfig, ObjectMapper parser) {
        return tryBowerOrNPMFormat("package.json", webjar, requireConfig, parser);
    }

    private boolean tryBowerOrNPMFormat(String configFile, WebJar webjar, ObjectNode requireConfig, ObjectMapper parser) {

        try {
            Resource configResource = webjar.createRelative(configFile);

            if (configResource.exists()) {
                try (InputStream is = configResource.getInputStream();) {
                    JsonNode config = parser.readTree(is);
                    ObjectNode paths = (ObjectNode) requireConfig.get("paths");
                    ArrayNode entries = paths.arrayNode();
                    String name = config.get("name").asText();
                    String path = webjar.getRootPath();

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
                    main = i < 0 ? main : main.substring(0, i);
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
     * @param webjar The web jar.
     * @param requireConfig The RequireJS configuration we are building.
     * @return True if successfully processed.
     * @throws Exception Unspecified exception
     */
    private boolean tryUnknownFormat(WebJar webjar, ObjectNode requireConfig) throws Exception {
        Resource resource = webjar.findResource(applicationContext, "js", "css");

        if (resource == null) {
            return false;
        }

        log.warn("Unknown web jar packaging, so inferring configuration for " + webjar);
        String main = resource.getURL().toString();
        String abs = webjar.getAbsolutePath();
        main = main.substring(abs.length());
        int i = main.lastIndexOf(".");
        main = i < 0 ? main : main.substring(0, i);
        ObjectNode paths = (ObjectNode) requireConfig.get("paths");
        ArrayNode entries = paths.arrayNode();
        entries.add(webjar.getRootPath() + main);
        paths.set(webjar.getModule(), entries);
        return true;
    }

}
