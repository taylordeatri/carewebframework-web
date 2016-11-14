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
package org.carewebframework.web.testharness;

import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;

public class BoxesController extends BaseController {
    
    @WiredComponent
    private Textbox txtSelect;
    
    @WiredComponent
    private Textbox txtInput;
    
    @WiredComponent
    private Listbox lboxRender;
    
    @WiredComponent
    private Combobox cboxRender;
    
    @Override
    public void afterInitialized(BaseComponent root) {
        super.afterInitialized(root);
        ListModel<String> model = new ListModel<>();
        
        for (int i = 1; i < 6; i++) {
            model.add("Rendered item #" + i);
        }
        
        lboxRender.getModelAndView(String.class).setModel(model);
        cboxRender.getModelAndView(String.class).setModel(model);
        
        lboxRender.getModelAndView(String.class).setRenderer(new IComponentRenderer<Listitem, String>() {
            
            @Override
            public Listitem render(String model) {
                return new Listitem(model);
            }
            
        });
        
        cboxRender.getModelAndView(String.class).setRenderer(new IComponentRenderer<Comboitem, String>() {
            
            @Override
            public Comboitem render(String model) {
                return new Comboitem(model);
            }
            
        });
    }
    
    @EventHandler(value = "select", target = "^.tabInputBoxes")
    private void InputBoxTabSelectHandler() {
        txtInput.setValue("Value set programmatically");
        txtSelect.selectRange(2, 5);
        txtSelect.focus();
    }
    
}
