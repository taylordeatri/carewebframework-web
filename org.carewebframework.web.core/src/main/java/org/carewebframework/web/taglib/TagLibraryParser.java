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
package org.carewebframework.web.taglib;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.XMLUtil;
import org.carewebframework.web.core.WebUtil;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Parses a tag library definition file.
 */
public class TagLibraryParser {
    
    private static final TagLibraryParser instance = new TagLibraryParser();
    
    public static TagLibraryParser getInstance() {
        return instance;
    }
    
    private TagLibraryParser() {
    }
    
    public TagLibrary parse(String src) {
        return parse(WebUtil.getResource(src));
    }
    
    public TagLibrary parse(Resource resource) {
        try {
            return parse(resource.getInputStream());
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    public TagLibrary parse(InputStream stream) {
        try {
            Element root = XMLUtil.parseXMLFromStream(stream).getDocumentElement();
            String uri = getValue(root, "uri");
            NodeList nodes = root.getElementsByTagName("function");
            int nodeCount = nodes.getLength();
            TagLibrary tagLibrary = new TagLibrary(uri);
            
            for (int i = 0; i < nodeCount; i++) {
                Element ele = (Element) nodes.item(i);
                String name = getValue(ele, "name");
                String clazz = getValue(ele, "function-class");
                String signature = getValue(ele, "function-signature");
                tagLibrary.addFunction(name, clazz, signature);
            }
            
            return tagLibrary;
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
    
    /**
     * Returns the text content of the specified tag.
     * 
     * @param ele The parent element of the desired tag.
     * @param tag The tag name.
     * @return The text content of the tag.
     */
    private String getValue(Element ele, String tag) {
        NodeList nodes = ele.getElementsByTagName(tag);
        
        if (nodes.getLength() == 0) {
            throw new RuntimeException("Tag library definition missing attribute: " + tag);
        }
        
        return nodes.item(0).getTextContent().trim();
    }
}
