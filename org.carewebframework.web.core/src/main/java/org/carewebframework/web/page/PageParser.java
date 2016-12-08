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
package org.carewebframework.web.page;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.XMLUtil;
import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.annotation.ComponentRegistry;
import org.carewebframework.web.core.WebUtil;
import org.carewebframework.web.taglib.TagLibrary;
import org.carewebframework.web.taglib.TagLibraryRegistry;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

public class PageParser {
    
    private static final PageParser instance = new PageParser();
    
    private static final String CONTENT_TAG = "#text";
    
    public static PageParser getInstance() {
        return instance;
    }
    
    private PageParser() {
    }
    
    public PageDefinition parse(String src) {
        return parse(WebUtil.getResource(src));
    }
    
    public PageDefinition parse(Resource resource) {
        try {
            return parse(resource.getInputStream());
        } catch (IOException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    public PageDefinition parse(InputStream stream) {
        try {
            Document document = XMLUtil.parseXMLFromStream(stream);
            PageDefinition pageDefinition = new PageDefinition();
            parseNode(document, pageDefinition.getRootElement());
            return pageDefinition;
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
    
    private void parseNode(Node node, PageElement parentElement) {
        ComponentDefinition def;
        PageElement childElement;
        ComponentDefinition parentDef = parentElement.getDefinition();
        
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                Element ele = (Element) node;
                String tag = ele.getTagName();
                def = ComponentRegistry.getInstance().get(tag);
                
                if (def == null) {
                    throw new RuntimeException("Unrecognized tag: " + tag);
                }
                
                childElement = new PageElement(def, parentElement);
                NamedNodeMap attributes = ele.getAttributes();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attr = attributes.item(i);
                    childElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                }
                
                parseChildren(node, childElement);
                childElement.validate();
                break;
            
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                Text text = (Text) node;
                String value = text.getWholeText();
                
                if (value.trim().isEmpty()) {
                    break;
                }
                
                switch (parentDef.contentHandling()) {
                    case ERROR:
                        throw new RuntimeException("Text content is not allowed for tag " + parentDef.getTag());
                        
                    case IGNORE:
                        break;
                    
                    case AS_ATTRIBUTE:
                        parentElement.setAttribute(CONTENT_TAG, normalizeText(value));
                        break;
                    
                    case AS_CHILD:
                        def = ComponentRegistry.getInstance().get(CONTENT_TAG);
                        childElement = new PageElement(def, parentElement);
                        childElement.setAttribute(CONTENT_TAG, normalizeText(value));
                        break;
                }
                
                break;
            
            case Node.DOCUMENT_NODE:
                parseChildren(node, parentElement);
                break;
            
            case Node.COMMENT_NODE:
                break;
            
            case Node.PROCESSING_INSTRUCTION_NODE:
                ProcessingInstruction pi = (ProcessingInstruction) node;
                
                if ("taglib".equals(pi.getTarget())) {
                    String uri = getAttribute(pi, "uri");
                    String prefix = getAttribute(pi, "prefix");
                    TagLibrary tagLibrary = TagLibraryRegistry.getInstance().get(uri);
                    parentElement.addTagLibrary(prefix, tagLibrary);
                }
                
                break;
            
            default:
                throw new RuntimeException("Unrecognized document content: " + node.getNodeName());
        }
    }
    
    private String getAttribute(ProcessingInstruction pi, String name) {
        String data = pi.getData();
        int i = data.indexOf(name + "=");
        data = data.substring(i + name.length() + 1);
        String dlm = data.startsWith("\"") || data.startsWith("'") ? data.substring(0, 1) : null;
        i = data.indexOf(dlm == null ? " " : dlm, 1);
        data = data.substring(dlm == null ? 0 : 1, i < 0 ? data.length() : i);
        data = data.isEmpty() ? null : data;
        
        if (data == null) {
            throw new IllegalArgumentException(
                    "Processing instruction \"" + pi.getTarget() + "\" is missing expected attribute \"" + name + "\"");
        }
        
        return data;
    }
    
    private void parseChildren(Node node, PageElement parentElement) {
        NodeList children = node.getChildNodes();
        int childCount = children.getLength();
        
        for (int i = 0; i < childCount; i++) {
            Node childNode = children.item(i);
            parseNode(childNode, parentElement);
        }
    }
    
    private String normalizeText(String text) {
        int i = text.indexOf('\n');
        
        if (i == -1) {
            return text;
        }
        
        if (text.substring(0, i).trim().isEmpty()) {
            text = text.substring(i + 1);
        }
        
        i = text.lastIndexOf('\n');
        
        if (i >= 0 && text.substring(i).trim().isEmpty()) {
            text = text.substring(0, i);
        }
        
        return text;
    }
    
}
