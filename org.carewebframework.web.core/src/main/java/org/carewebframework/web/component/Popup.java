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

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.ContentHandling;

/**
 * A container component that be displayed at a selected location.
 */
@Component(value = "popup", widgetClass = "Popup", content = ContentHandling.AS_CHILD, parentTag = "*", childTag = @ChildTag("*"))
public class Popup extends BaseComponent implements INamespace {
    
    public void open(BaseComponent reference) {
        open(reference, null, null);
    }
    
    public void open(BaseComponent reference, String my, String at) {
        Map<String, Object> map = new HashMap<>();
        map.put("of", reference);
        map.put("at", at);
        map.put("my", my);
        invoke("open", map, true);
    }
    
    public void close() {
        invoke("close", true);
    }
}
