package org.carewebframework.web.codemirror;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.BaseInputComponent;

/**
 * CWF wrapper for HighCharts component.
 */
@Component(value = "codemirror", widgetModule = "cwf-codemirror", widgetClass = "CodeMirror", parentTag = "*")
public class CodeMirror extends BaseInputComponent<String> {
    
    private String mode;
    
    private boolean lineNumbers;
    
    private String placeholder;
    
    private boolean readonly;
    
    public void format() {
        invoke("format");
    }
    
    @PropertyGetter("readonly")
    public boolean isReadonly() {
        return readonly;
    }
    
    @PropertySetter("readonly")
    public void setReadonly(boolean readonly) {
        if (readonly != this.readonly) {
            sync("readonly", this.readonly = readonly);
        }
    }
    
    @PropertyGetter("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }
    
    @PropertySetter("placeholder")
    public void setPlaceholder(String placeholder) {
        if (!areEqual(placeholder = nullify(placeholder), this.placeholder)) {
            sync("placeholder", this.placeholder = placeholder);
        }
    }
    
    @PropertyGetter("mode")
    public String getMode() {
        return mode;
    }
    
    @PropertySetter("mode")
    public void setMode(String mode) {
        if (!areEqual(mode = trimify(mode), this.mode)) {
            sync("mode", this.mode = mode);
        }
    }
    
    @PropertyGetter("lineNumbers")
    public boolean getLineNumbers() {
        return lineNumbers;
    }
    
    @PropertySetter("lineNumbers")
    public void setLineNumbers(boolean lineNumbers) {
        if (lineNumbers != this.lineNumbers) {
            sync("lineNumbers", this.lineNumbers = lineNumbers);
        }
    }
    
    @Override
    protected String _toValue(String value) {
        return value;
    }
    
    @Override
    protected String _toString(String value) {
        return value;
    }
}
