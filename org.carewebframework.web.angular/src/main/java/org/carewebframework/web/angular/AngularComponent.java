package org.carewebframework.web.angular;

import org.carewebframework.web.annotation.Component;
import org.carewebframework.web.annotation.Component.PropertyGetter;
import org.carewebframework.web.annotation.Component.PropertySetter;
import org.carewebframework.web.component.BaseUIComponent;

@Component(value = "angular", widgetPackage = "cwf-angular-widget", widgetClass = "AngularWidget", parentTag = "*")
public class AngularComponent extends BaseUIComponent {
    
    private String src;

    @PropertyGetter("src")
    public String getSrc() {
        return src;
    }

    @PropertySetter("src")
    public void setSrc(String src) {
        if (!areEqual(src = trimify(src), this.src)) {
            sync("src", this.src = src);
        }
    }

}
