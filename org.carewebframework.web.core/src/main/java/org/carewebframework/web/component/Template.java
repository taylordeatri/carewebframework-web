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

import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.page.PageParser;

@Component(value = "template", widgetClass = "Span", parentTag = "*", childTag = @ChildTag("snippet"))
public class Template extends BaseComponent implements INamespace {
    
    public Template() {
    }
    
    public Template(String src) {
        setSrc(src);
    }
    
    /**
     * Snippets are handled differently from other children. They are never really added as
     * children. Rather, the component tree resulting from the materialization of the snippet's
     * source is attached to the anchor point specified by the snippet.
     */
    @Override
    public void addChild(BaseComponent child, int index) {
        if (child instanceof Snippet) {
            ((Snippet) child).materialize(this);
        } else {
            super.addChild(child, index);
        }
    }
    
    /**
     * We override this because the schema constrains children to snippets only, but we want to be
     * able to dynamically add children of any type.
     */
    @Override
    protected void validateChild(BaseComponent child) {
        // NOP
    }
    
    @PropertySetter(value = "src")
    private void setSrc(String src) {
        src = nullify(src);
        
        if (src != null) {
            PageParser.getInstance().parse(src).materialize(this);
        }
    }
}
