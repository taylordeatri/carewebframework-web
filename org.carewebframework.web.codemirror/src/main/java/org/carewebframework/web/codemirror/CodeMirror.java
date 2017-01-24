package org.carewebframework.web.codemirror;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.Memobox;

/**
 * CWF wrapper for HighCharts component.
 */
@Component(value = "codemirror", widgetPackage = "cwf-codemirror", widgetClass = "CodeMirror", parentTag = "*")
public class CodeMirror extends Memobox {
    
    private String mode;
    
    private boolean lineNumbers;
    
    public void format() {
        invoke("format");
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
}
