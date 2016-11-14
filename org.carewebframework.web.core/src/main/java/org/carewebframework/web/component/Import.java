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

import javax.servlet.ServletContext;

import org.carewebframework.common.MiscUtil;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.page.PageParser;

/**
 * Import component permits importing another page into the referencing page. Note that while it
 * prohibits child tags, it does allow the addition of child components either programmatically or
 * via the specified source page.
 */
@Component(value = "import", widgetClass = "Span", parentTag = "*")
public class Import extends BaseUIComponent {
    
    private String src;
    
    @Override
    public boolean isContainer() {
        return true;
    }
    
    @Override
    protected void validateChild(BaseComponent child) {
        child.getDefinition().validateParent(getDefinition());
    }
    
    @PropertyGetter("src")
    public String getSrc() {
        return src;
    }
    
    @PropertySetter(value = "src", defer = true)
    public void setSrc(String src) {
        if (!areEqual(src = nullify(src), this.src)) {
            this.src = src;
            this.destroyChildren();
            
            if (src != null) {
                try {
                    ServletContext ctx = ExecutionContext.getSession().getServletContext();
                    String path = ctx.getResource(src).toString();
                    PageParser.getInstance().parse(path).materialize(this);
                } catch (Exception e) {
                    throw MiscUtil.toUnchecked(e);
                }
            }
        }
    }
}
