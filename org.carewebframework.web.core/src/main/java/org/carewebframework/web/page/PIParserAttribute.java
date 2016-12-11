package org.carewebframework.web.page;

import org.w3c.dom.ProcessingInstruction;

/**
 * Parser for processing instructions that specify custom component attributes.
 */
public class PIParserAttribute extends PIParserBase {
    
    public PIParserAttribute() {
        super("attribute");
    }
    
    @Override
    public void parse(ProcessingInstruction pi, PageElement element) {
        String key = getAttribute(pi, "key", true);
        String value = getAttribute(pi, "value", true);
        element.setAttribute("@" + key, value);
    }
    
}
