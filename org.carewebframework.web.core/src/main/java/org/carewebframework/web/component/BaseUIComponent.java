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

import org.carewebframework.web.ancillary.ConvertUtil;
import org.carewebframework.web.ancillary.CssClasses;
import org.carewebframework.web.ancillary.CssStyles;
import org.carewebframework.web.ancillary.IDisable;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;

public abstract class BaseUIComponent extends BaseComponent implements IDisable {
    
    private final CssStyles styles = new CssStyles();
    
    private final CssClasses classes = new CssClasses();
    
    private String height;
    
    private String width;
    
    private String hint;
    
    private String balloon;
    
    private boolean disabled;
    
    private boolean visible = true;
    
    private int tabindex;
    
    private String css;
    
    private String dragid;
    
    private String dropid;
    
    private Popup context;
    
    public void addMask() {
        addMask(null);
    }
    
    public void addMask(String label) {
        sync("mask", label);
    }
    
    public void removeMask() {
        sync("mask", false);
    }
    
    @PropertyGetter("style")
    public String getStyles() {
        return styles.toString();
    }
    
    @PropertySetter("style")
    public void setStyles(String styles) {
        this.styles.parse(styles, true);
        _syncStyles();
    }
    
    private String _syncStyle(String name, String dflt) {
        String current = styles.get(name);
        
        if (current != null) {
            return current;
        }
        
        styles.put(name, dflt);
        return dflt;
    }
    
    protected void _syncStyles() {
        height = _syncStyle("height", height);
        width = _syncStyle("width", width);
        sync("style", styles.toString());
    }
    
    public String getStyle(String name) {
        return styles.get(name);
    }
    
    public String addStyle(String name, String value) {
        String oldValue = styles.put(name, value);
        _syncStyles();
        return oldValue;
    }
    
    public void addStyles(String style) {
        styles.parse(style, false);
        _syncStyles();
    }
    
    public String removeStyle(String name) {
        return addStyle(name, null);
    }
    
    @PropertyGetter("class")
    public String getClasses() {
        return classes.toString();
    }
    
    public void setClasses(String value) {
        classes.parse(value);
        _syncClasses();
    }
    
    protected void _syncClasses() {
        sync("clazz", classes.toString(true));
    }
    
    @PropertySetter("class")
    public void addClass(String value) {
        if (classes.add(value)) {
            _syncClasses();
        }
    }
    
    public void removeClass(String value) {
        if (classes.remove(value)) {
            _syncClasses();
        }
    }
    
    public void toggleClass(String yesValue, String noValue, boolean condition) {
        if (classes.toggle(yesValue, noValue, condition)) {
            _syncClasses();
        }
    }
    
    public void hide() {
        setVisible(false);
    }
    
    public void show() {
        setVisible(true);
    }
    
    @PropertyGetter("height")
    public String getHeight() {
        return height;
    }
    
    @PropertySetter("height")
    public void setHeight(String height) {
        height = trimify(height);
        
        if (!areEqual(height, this.height)) {
            this.height = height;
            addStyle("height", height);
        }
    }
    
    @PropertyGetter("width")
    public String getWidth() {
        return width;
    }
    
    @PropertySetter("width")
    public void setWidth(String width) {
        width = trimify(width);
        
        if (!areEqual(width, this.width)) {
            this.width = width;
            addStyle("width", width);
        }
    }
    
    @PropertySetter(value = "focus", defer = true)
    public void setFocus(boolean focus) {
        invoke("focus", focus);
    }
    
    public void focus() {
        setFocus(true);
    }
    
    @PropertyGetter("css")
    public String getCss() {
        return css;
    }
    
    @PropertySetter("css")
    public void setCss(String css) {
        if (!areEqual(css = nullify(css), this.css)) {
            sync("css", this.css = css);
        }
    }
    
    @PropertyGetter("hint")
    public String getHint() {
        return hint;
    }
    
    @PropertySetter("hint")
    public void setHint(String hint) {
        if (!areEqual(hint = nullify(hint), this.hint)) {
            sync("hint", this.hint = hint);
        }
    }
    
    @PropertyGetter("balloon")
    public String getBalloon() {
        return balloon;
    }
    
    @PropertySetter("balloon")
    public void setBalloon(String balloon) {
        if (!areEqual(balloon, this.balloon)) {
            sync("balloon", this.balloon = balloon);
        }
    }
    
    @Override
    @PropertyGetter("disabled")
    public boolean isDisabled() {
        return disabled;
    }
    
    @Override
    @PropertySetter("disabled")
    public void setDisabled(boolean disabled) {
        if (disabled != this.disabled) {
            sync("disabled", this.disabled = disabled);
        }
    }
    
    @PropertyGetter("visible")
    public boolean isVisible() {
        return visible;
    }
    
    @PropertySetter("visible")
    public void setVisible(boolean visible) {
        if (visible != this.visible) {
            sync("visible", this.visible = visible);
        }
    }
    
    @PropertyGetter("tabindex")
    public int getTabindex() {
        return tabindex;
    }
    
    @PropertySetter("tabindex")
    public void setTabindex(int tabindex) {
        tabindex = tabindex < 0 ? 0 : tabindex;
        
        if (tabindex != this.tabindex) {
            sync("tabindex", this.tabindex = tabindex);
        }
    }
    
    @PropertyGetter("dragid")
    public String getDragid() {
        return dragid;
    }
    
    @PropertySetter("dragid")
    public void setDragid(String dragid) {
        dragid = trimify(dragid);
        
        if (!areEqual(dragid, this.dragid)) {
            sync("dragid", this.dragid = dragid);
        }
    }
    
    @PropertyGetter("dropid")
    public String getDropid() {
        return dropid;
    }
    
    @PropertySetter("dropid")
    public void setDropid(String dropid) {
        dropid = trimify(dropid);
        
        if (!areEqual(dropid, this.dropid)) {
            sync("dropid", this.dropid = dropid);
        }
    }
    
    @PropertyGetter("context")
    public Popup getContext() {
        if (context != null && context.isDead()) {
            context = null;
            sync("context", context);
        }
        
        return context;
    }
    
    @PropertySetter(value = "context", defer = true)
    private void setContext(String context) {
        setContext(ConvertUtil.convert(context, Popup.class, this));
    }
    
    public void setContext(Popup context) {
        if (context != getContext()) {
            validate(context);
            sync("context", this.context = context);
        }
    }
    
    public void scrollIntoView(boolean alignToTop) {
        invoke("scrollIntoView", alignToTop);
    }
}
