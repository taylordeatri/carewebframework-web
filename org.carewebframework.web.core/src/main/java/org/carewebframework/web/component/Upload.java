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
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.springframework.util.Assert;

/**
 * A component for uploading files to the server.
 */
@Component(value = "upload", widgetModule = "cwf-upload", widgetClass = "Upload", parentTag = "*")
public class Upload extends BaseUIComponent {
    
    private boolean multiple;
    
    private boolean progress;
    
    private String accept;
    
    private int maxsize = 1024 * 1024 * 100;
    
    public Upload() {
        super();
    }
    
    /**
     * Returns true if multiple file uploads are allowed.
     * 
     * @return True if multiple file uploads are allowed.
     */
    @PropertyGetter("multiple")
    public boolean isMultiple() {
        return multiple;
    }
    
    /**
     * Set to true if multiple file uploads are allowed.
     * 
     * @param multiple True if multiple file uploads are to be allowed.
     */
    @PropertySetter("multiple")
    public void setMultiple(boolean multiple) {
        if (multiple != this.multiple) {
            sync("multiple", this.multiple = multiple);
        }
    }
    
    /**
     * Returns the specifier reflecting the type of files the server expects.
     * 
     * @return Specifier for acceptable file types.
     */
    @PropertyGetter("accept")
    public String getAccept() {
        return accept;
    }
    
    /**
     * Sets the specifier reflecting the type of files the server expects.
     * 
     * @param accept Specifier for acceptable file types. Valid values include:
     *            <ul>
     *            <li>A file extension prefixed with a period.</li>
     *            <li>audio/* = Any audio file.</li>
     *            <li>video/* = Any video file.</li>
     *            <li>image/* = Any image file.</li>
     *            <li>Any valid IANA media type.</li>
     *            </ul>
     */
    @PropertySetter("accept")
    public void setAccept(String accept) {
        if (!areEqual(accept = nullify(accept), this.accept)) {
            sync("accept", this.accept = accept);
        }
    }
    
    /**
     * Returns the maximum allowable file size, in bytes. Any attempt to upload a file larger than
     * this size will produce an exception.
     * 
     * @return The maximum allowable file size, in bytes.
     */
    @PropertyGetter("maxsize")
    public int getMaxsize() {
        return maxsize;
    }
    
    /**
     * Sets the maximum allowable file size, in bytes.
     * 
     * @param maxsize The maximum allowable file size, in bytes. Any attempt to upload a file larger
     *            than this size will produce an exception.
     */
    @PropertySetter("maxsize")
    public void setMaxsize(int maxsize) {
        if (maxsize != this.maxsize) {
            Assert.isTrue(maxsize >= 0, "maxsize must be >= 0");
            sync("_maxsize", this.maxsize = maxsize);
        }
    }
    
    /**
     * If true, the uploader will fire UploadEvent events to report progress.
     * 
     * @see org.carewebframework.web.event.UploadEvent
     * @return Returns true if UploadEvent events will be fired.
     */
    @PropertyGetter("progress")
    public boolean getProgress() {
        return progress;
    }
    
    /**
     * Set to true to receive upload progress events.
     * 
     * @see org.carewebframework.web.event.UploadEvent
     * @param progress True if UploadEvent events are to be fired.
     */
    @PropertySetter("progress")
    public void setProgress(boolean progress) {
        if (progress != this.progress) {
            sync("_progress", this.progress = progress);
        }
    }
    
    /**
     * Abort all file uploads in progress.
     */
    public void abortAll() {
        invokeIfAttached("abortAll");
    }
    
    /**
     * Abort an upload for a specific file.
     * 
     * @param filename File whose upload is to be aborted.
     */
    public void abort(String filename) {
        invokeIfAttached("abort", filename);
    }
    
    /**
     * Bind uploader to another component. A click event on that component will then trigger an
     * upload.
     * 
     * @param comp Component to bind.
     */
    public void bind(BaseUIComponent comp) {
        invoke("bind", comp);
    }
    
    /**
     * Unbind a previously bound component.
     * 
     * @param comp Component to unbind.
     */
    public void unbind(BaseUIComponent comp) {
        invoke("unbind", comp);
    }
    
}
