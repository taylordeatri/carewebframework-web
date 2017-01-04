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

import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.annotation.WiredComponent;
import org.carewebframework.web.component.Button;
import org.carewebframework.web.component.Checkbox;
import org.carewebframework.web.component.Radiobutton;
import org.carewebframework.web.component.Upload;
import org.carewebframework.web.event.ChangeEvent;
import org.carewebframework.web.event.UploadEvent;

/*
 * Buttons demonstration.
 */
public class ButtonsController extends BaseController {
    
    @WiredComponent
    private Button btnWithEvent;
    
    @WiredComponent
    private Checkbox chkMultiple;
    
    @WiredComponent
    private Upload upload;
    
    /**
     * Sample button event handler.
     */
    @EventHandler(value = "click", target = "@btnWithEvent")
    private void btnEventHandler() {
        log("Button event handler was invoked");
    }
    
    @EventHandler(value = "change", target = { "rg1", "rg2" })
    private void radiobuttonChangeHandler(ChangeEvent event) {
        boolean isSelected = event.getValue(Boolean.class);
        log("Radiobutton '" + ((Radiobutton) event.getTarget()).getLabel() + "' was "
                + (isSelected ? "selected." : "deselected."));
    }
    
    @EventHandler(value = "upload", target = "@upload")
    private void uploadHandler(UploadEvent event) throws Exception {
        String file = event.getFile();
        
        switch (event.getState()) {
            case DONE:
                String tmpdir = System.getProperty("java.io.tmpdir");
                file = tmpdir + file;
                FileOutputStream out = new FileOutputStream(file);
                IOUtils.copy(event.getBlob(), out);
                out.close();
                log("Uploaded contents to " + file);
                break;
            
            case MAXSIZE:
                log("File too large: " + file);
                break;
            
            case ABORTED:
                log("Upload aborted for " + file);
                break;
            
            case LOADING:
                double pct = event.getLoaded() * 100.0 / event.getTotal();
                log("Upload " + pct + "% completed for " + file);
                break;
        }
    }
    
    @EventHandler(value = "change", target = "@chkMultiple")
    private void chkMultipleChangeHandler(ChangeEvent event) {
        upload.setMultiple(chkMultiple.isChecked());
    }
}
