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
package org.carewebframework.web.component;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageParser;
import org.springframework.util.Assert;

/**
 * Represents a reference to cwf resource that can be inserted into a template.
 */
@Component(value = "snippet", widgetClass = "MetaWidget", parentTag = "template")
public class Snippet extends BaseComponent {
    
    private enum AnchorPosition {
        BEFORE, AFTER, CHILD
    };
    
    private String src;
    
    private String anchor;
    
    private AnchorPosition position = AnchorPosition.CHILD;
    
    public Snippet() {
    }
    
    /*package*/ void materialize(Template template) {
        Assert.isTrue(src != null && anchor != null, "A snippet requires both a src and an anchor");
        BaseComponent ref = template.findByName(anchor);
        Assert.notNull(ref, "Could not locate anchor for snippet at " + anchor);
        PageDefinition def = PageParser.getInstance().parse(src);
        
        if (position == AnchorPosition.CHILD) {
            def.materialize(ref);
        } else {
            ref = ref.getParent();
            Assert.notNull(ref, "Anchor must have a parent for position value of " + position);
            
            for (BaseComponent child : def.materialize(null)) {
                ref.addChild(child, position == AnchorPosition.BEFORE ? 0 : -1);
            }
        }
    }
    
    @PropertySetter(value = "src")
    private void setSrc(String src) {
        this.src = trimify(src);
    }
    
    @PropertySetter(value = "anchor")
    private void setAnchor(String anchor) {
        this.anchor = trimify(anchor);
    }
    
    @PropertySetter(value = "position")
    private void setPosition(AnchorPosition position) {
        this.position = position == null ? AnchorPosition.CHILD : position;
    }
}
