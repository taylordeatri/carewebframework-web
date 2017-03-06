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
import org.carewebframework.web.annotation.OnFailure;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.BaseLabeledComponent.LabelPositionAll;
import org.carewebframework.web.component.Caption;
import org.carewebframework.web.component.Caption.LabelAlignment;
import org.carewebframework.web.component.Combobox;
import org.carewebframework.web.component.Comboitem;
import org.carewebframework.web.component.Listbox;
import org.carewebframework.web.component.Listitem;
import org.carewebframework.web.component.Popupbox;
import org.carewebframework.web.component.Radiogroup;
import org.carewebframework.web.component.Textbox;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.KeycaptureEvent;
import org.carewebframework.web.model.IComponentRenderer;
import org.carewebframework.web.model.ListModel;

/**
 * Input boxes demonstration
 */
public class BoxesController extends BaseController {

    @WiredComponent
    private Textbox txtSelect;

    @WiredComponent
    private Textbox txtInput;

    @WiredComponent
    private Listbox lboxRender;

    @WiredComponent
    private Combobox cboxRender;

    @WiredComponent
    Popupbox popupbox;

    @WiredComponent
    Caption caption;

    @WiredComponent
    Radiogroup rgPosition;

    @WiredComponent
    Radiogroup rgAlignment;

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

    @EventHandler(value = "change", target = "tabInputBoxes", onFailure = OnFailure.LOG)
    private void InputBoxTabChangeHandler() {
        txtInput.setValue("Value set programmatically");
        txtSelect.selectRange(2, 5);
        txtSelect.focus();
    }

    @EventHandler(value = "close", target = "popupboxpopup")
    private void popupboxCloseHandler(Event event) {
        popupbox.setValue("Drop down closed!");
    }

    @EventHandler(value = "open", target = "popupboxpopup")
    private void popupboxOpenHandler(Event event) {
        popupbox.setValue("Drop down opened!");
    }

    @EventHandler(value = "keycapture", target = "memobox")
    private void memoboxKeyPressHandler(KeycaptureEvent event) {
        log("Captured keypress: " + event.getKeycapture());
    }

    @EventHandler(value = "change", target = "@rgPosition")
    private void positionChangeHandler() {
        String value = rgPosition.getSelected().getLabel();
        LabelPositionAll position = LabelPositionAll.valueOf(value.toUpperCase());
        caption.setPosition(position);
    }

    @EventHandler(value = "change", target = "@rgAlignment")
    private void alignmentChangeHandler() {
        String value = rgAlignment.getSelected().getLabel();
        LabelAlignment alignment = LabelAlignment.valueOf(value.toUpperCase());
        caption.setAlignment(alignment);
    }
}
