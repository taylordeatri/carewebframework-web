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

import java.util.function.BooleanSupplier;

import org.carewebframework.web.ancillary.INamespace;
import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.ChildTag;
import org.carewebframework.web.annotation.Component.ContentHandling;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.annotation.EventHandler;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.event.Event;
import org.carewebframework.web.event.IEventListener;

/**
 * A window component with a title bar and maximize/minimize/close buttons. May be used in modal,
 * popup, or inline modes.
 */
@Component(value = "window", widgetClass = "Window", content = ContentHandling.AS_CHILD, parentTag = "*", childTag = @ChildTag("*"))
public class Window extends BaseUIComponent implements INamespace {
    
    public enum Mode {
        MODAL, INLINE, POPUP;
    }
    
    public enum Size {
        NORMAL, MAXIMIZED, MINIMIZED;
    }
    
    public enum CloseAction {
        DETACH, DESTROY, HIDE
    }
    
    private String title;
    
    private String image;
    
    private boolean closable;
    
    private boolean movable = true;
    
    private boolean sizable;
    
    private boolean maximizable;
    
    private boolean minimizable;
    
    private Size size = Size.NORMAL;
    
    private BooleanSupplier onCanClose;
    
    private CloseAction closeAction = CloseAction.DESTROY;
    
    private Mode mode;
    
    private IEventListener closeListener;
    
    public Window() {
        super();
        setMode(Mode.INLINE);
        addClass("flavor:panel-default");
    }
    
    @PropertyGetter("title")
    public String getTitle() {
        return title;
    }
    
    @PropertySetter("title")
    public void setTitle(String title) {
        if (!areEqual(title, this.title)) {
            sync("title", this.title = title);
        }
    }
    
    @PropertyGetter("image")
    public String getImage() {
        return image;
    }
    
    @PropertySetter("image")
    public void setImage(String image) {
        if (!areEqual(image = nullify(image), this.image)) {
            sync("image", this.image = image);
        }
    }
    
    @PropertyGetter("closable")
    public boolean isClosable() {
        return closable;
    }
    
    @PropertySetter("closable")
    public void setClosable(boolean closable) {
        if (closable != this.closable) {
            sync("closable", this.closable = closable);
        }
    }
    
    @PropertyGetter("sizable")
    public boolean isSizable() {
        return sizable;
    }
    
    @PropertySetter("sizable")
    public void setSizable(boolean sizable) {
        if (sizable != this.sizable) {
            sync("sizable", this.sizable = sizable);
        }
    }
    
    @PropertyGetter("movable")
    public boolean isMovable() {
        return movable;
    }
    
    @PropertySetter("movable")
    public void setMovable(boolean movable) {
        if (movable != this.movable) {
            sync("movable", this.movable = movable);
        }
    }
    
    @PropertyGetter("maximizable")
    public boolean isMaximizable() {
        return maximizable;
    }
    
    @PropertySetter("maximizable")
    public void setMaximizable(boolean maximizable) {
        if (maximizable != this.maximizable) {
            sync("maximizable", this.maximizable = maximizable);
        }
    }
    
    @PropertyGetter("minimizable")
    public boolean isMinimizable() {
        return minimizable;
    }
    
    @PropertySetter("minimizable")
    public void setMinimizable(boolean minimizable) {
        if (minimizable != this.minimizable) {
            sync("minimizable", this.minimizable = minimizable);
        }
    }
    
    @PropertyGetter("mode")
    public Mode getMode() {
        return mode;
    }
    
    @PropertySetter("mode")
    public void setMode(Mode mode) {
        mode = mode == null ? Mode.INLINE : mode;
        
        if (mode != this.mode) {
            sync("mode", this.mode = mode);
        }
    }
    
    @PropertyGetter("size")
    public Size getSize() {
        return size;
    }
    
    @PropertySetter("size")
    public void setSize(Size size) {
        size = size == null ? Size.NORMAL : size;
        
        if (size != this.size) {
            sync("size", this.size = size);
        }
    }
    
    @PropertyGetter("closeAction")
    public CloseAction getCloseAction() {
        return closeAction;
    }
    
    @PropertySetter("closeAction")
    public void setCloseAction(CloseAction closeAction) {
        this.closeAction = closeAction == null ? CloseAction.DESTROY : closeAction;
    }
    
    private boolean canClose() {
        return onCanClose == null || onCanClose.getAsBoolean();
    }
    
    public void close() {
        if (canClose()) {
            switch (closeAction) {
                case DESTROY:
                    destroy();
                    break;
                
                case DETACH:
                    detach();
                    break;
                
                case HIDE:
                    setVisible(false);
                    break;
            }
            
            if (closeListener != null) {
                try {
                    closeListener.onEvent(new Event("close", this));
                } finally {
                    closeListener = null;
                }
            }
        }
    }
    
    public BooleanSupplier getOnCanClose() {
        return onCanClose;
    }
    
    public void setOnCanClose(BooleanSupplier onCanClose) {
        this.onCanClose = onCanClose;
    }
    
    public void modal(IEventListener closeListener) {
        doShow(Mode.MODAL, closeListener);
    }
    
    public void popup(IEventListener closeListener) {
        doShow(Mode.POPUP, closeListener);
    }
    
    private void doShow(Mode mode, IEventListener closeListener) {
        if (this.closeListener != null) {
            throw new IllegalStateException("Window is already open.");
        }
        
        if (getParent() == null) {
            setParent(ExecutionContext.getPage());
        }
        
        this.closeListener = closeListener;
        setMode(mode);
        setVisible(true);
        fireEvent("open");
    }
    
    @EventHandler(value = "close", syncToClient = false)
    private void _close(Event event) {
        close();
    }
    
}
