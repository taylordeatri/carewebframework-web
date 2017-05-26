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
package org.carewebframework.web.annotation;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.carewebframework.common.XMLUtil;
import org.carewebframework.web.ancillary.ComponentRegistry;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.ComponentDefinition.Cardinality;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Generate an XML schema from annotations.
 */
public class SchemaGenerator {

    private final Document schema;

    private static final String NS_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private static final String NS_VERSIONING = "http://www.w3.org/2007/XMLSchema-versioning";

    private static final String[] DEFAULT_PACKAGES = { "org.carewebframework.web.component" };

    /**
     * Main entry point.
     *
     * @param args The command line arguments.
     * @throws Exception Unspecified exception.
     */
    public static void main(String... args) throws Exception {
        Options options = new Options();
        Option option = new Option("p", "package", true, "Java package(s) to scan (default: " + DEFAULT_PACKAGES[0] + ")");
        option.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(option);
        option = new Option("v", false, "Schema v1.0 compatible (default: v1.1 compatible)");
        options.addOption(option);
        option = new Option("h", "help", false, "This help message");
        options.addOption(option);
        CommandLine cmd = new DefaultParser().parse(options, args);

        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp("SchemaGenerator [options] ...", options);
            return;
        }

        String[] packages = cmd.hasOption("p") ? cmd.getOptionValues("p") : DEFAULT_PACKAGES;
        String xml = new SchemaGenerator(packages, cmd.hasOption("v")).toString();
        String output = cmd.getArgs().length == 0 ? null : cmd.getArgs()[0];

        if (output == null) {
            System.out.println(xml);
        } else {
            try (OutputStream strm = new FileOutputStream(output)) {
                IOUtils.write(xml, strm, StandardCharsets.UTF_8);
            }
        }
    }

    public SchemaGenerator(String[] packages, boolean v1_0_compatible) throws Exception {
        ComponentRegistry registry = ComponentRegistry.getInstance();

        for (String pkg : packages) {
            ComponentScanner.getInstance().scanPackage(pkg);
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        schema = docBuilder.newDocument();
        Element root = schema.createElementNS(NS_SCHEMA, "xs:schema");
        root.setAttribute("targetNamespace", "http://www.carewebframework.org/schema/cwf");
        root.setAttributeNS(NS_VERSIONING, "vc:minVersion", v1_0_compatible ? "1.0" : "1.1");
        root.setAttribute("elementFormDefault", "qualified");
        schema.appendChild(root);

        for (ComponentDefinition def : registry) {
            if (def.getTag().startsWith("#")) {
                continue;
            }

            Element ele = createElement("element", root, "name", def.getTag());
            Element ct = createElement("complexType", ele);

            boolean childrenAllowed = def.childrenAllowed();
            boolean contentAllowed = def.contentHandling() != ContentHandling.ERROR;

            if (!childrenAllowed && contentAllowed) {
                Element sc = createElement("simpleContent", ct);
                ct = createElement("extension", sc);
                ct.setAttribute("base", "xs:string");
            } else if (childrenAllowed) {
                if (contentAllowed) {
                    ct.setAttribute("mixed", "true");
                }

                Element childAnchor;

                if (v1_0_compatible) {
                    childAnchor = createElement("choice", ct);
                    childAnchor.setAttribute("minOccurs", "0");
                    childAnchor.setAttribute("maxOccurs", "unbounded");
                } else {
                    childAnchor = createElement("all", ct);
                }

                for (Entry<String, Cardinality> childTag : def.getChildTags().entrySet()) {
                    String tag = childTag.getKey();
                    Cardinality card = childTag.getValue();

                    if (!"*".equals(tag)) {
                        ComponentDefinition childDef = registry.get(tag);
                        addChildElement(childAnchor, childDef, def, card);
                    } else {
                        for (ComponentDefinition childDef : registry) {
                            addChildElement(childAnchor, childDef, def, card);
                        }
                    }

                }
            }

            processAttributes(def.getSetters(), ct);
            processAttributes(def.getFactoryParameters(), ct);
        }
    }

    @Override
    public String toString() {
        return XMLUtil.toString(schema);
    }

    private void processAttributes(Map<String, Method> setters, Element ct) {
        for (Entry<String, Method> setter : setters.entrySet()) {
            if (setter.getKey().startsWith("#")) {
                continue;
            }

            Element attr = createElement("attribute", ct, "name", setter.getKey());
            Class<?> javaType = setter.getValue().getParameterTypes()[0];

            if (javaType.isEnum()) {
                processEnum(attr, javaType);
            } else {
                attr.setAttribute("type", getType(javaType));
            }
        }
    }

    private void addChildElement(Element seq, ComponentDefinition childDef, ComponentDefinition parentDef,
                                 Cardinality card) {

        if (childDef.getTag().startsWith("#")) {
            return;
        }

        if (childDef != null && !childDef.isParentTag(parentDef.getTag())) {
            return;
        }

        Element child = createElement("element", seq, "ref", childDef.getTag());
        child.setAttribute("minOccurs", Integer.toString(card.getMinimum()));

        if (card.hasMaximum()) {
            child.setAttribute("maxOccurs", Integer.toString(card.getMaximum()));
        } else {
            child.setAttribute("maxOccurs", "unbounded");
        }
    }

    private void processEnum(Element attr, Class<?> javaType) {
        Element st = createElement("simpleType", attr);
        Element res = createElement("restriction", st);
        res.setAttribute("base", "xs:string");

        for (Object val : javaType.getEnumConstants()) {
            createElement("enumeration", res, "value", val.toString().toLowerCase());
        }
    }

    private String getType(Class<?> javaType) {
        String type = null;
        type = type != null ? type : getType(javaType, "xs:boolean", boolean.class, Boolean.class);
        type = type != null ? type : getType(javaType, "xs:integer", int.class, Integer.class);
        type = type != null ? type : getType(javaType, "xs:decimal", float.class, Float.class, double.class, Double.class);
        return type != null ? type : "xs:string";
    }

    private String getType(Class<?> javaType, String type, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            if (clazz.isAssignableFrom(javaType)) {
                return type;
            }
        }

        return null;
    }

    private Element createElement(String tag, Element parent) {
        return createElement(tag, parent, null, null);
    }

    private Element createElement(String tag, Element parent, String keyName, String keyValue) {
        Element element = schema.createElement("xs:" + tag);

        if (keyName != null) {
            NodeList nodes = parent.getChildNodes();
            element.setAttribute(keyName, keyValue);

            for (int i = 0, j = nodes.getLength(); i < j; i++) {
                Element sib = (Element) nodes.item(i);
                String val = sib.getAttribute(keyName);

                if (val != null && val.compareToIgnoreCase(keyValue) >= 0) {
                    parent.insertBefore(element, sib);
                    return element;
                }
            }
        }

        parent.appendChild(element);
        return element;
    }
}
