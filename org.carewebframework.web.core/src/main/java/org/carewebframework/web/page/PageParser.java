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

import java.net.MalformedURLException;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.common.XMLUtil;
import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.annotation.ComponentRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class PageParser {
    
    private static final PageParser instance = new PageParser();
    
    private static final String CONTENT_TAG = "#text";
    
    public static PageParser getInstance() {
        return instance;
    }
    
    private PageParser() {
    }
    
    public PageDefinition parse(String url) {
        try {
            return parse(new UrlResource(url));
        } catch (MalformedURLException e) {
            throw MiscUtil.toUnchecked(e);
        }
    }
    
    public PageDefinition parse(Resource resource) {
        try {
            Document document = XMLUtil.parseXMLFromStream(resource.getInputStream());
            PageDefinition pageDefinition = new PageDefinition();
            parseNode(document.getDocumentElement(), pageDefinition.getRootElement());
            return pageDefinition;
        } catch (Exception e) {
            throw MiscUtil.toUnchecked(e);
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
                
                NodeList children = ele.getChildNodes();
                int childCount = children.getLength();
                
                for (int i = 0; i < childCount; i++) {
                    Node childNode = children.item(i);
                    parseNode(childNode, childElement);
                }
                
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
            
            case Node.COMMENT_NODE:
                break;
            
            case Node.PROCESSING_INSTRUCTION_NODE:
                break;
            
            default:
                throw new RuntimeException("Unrecognized document content: " + node.getNodeName());
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
