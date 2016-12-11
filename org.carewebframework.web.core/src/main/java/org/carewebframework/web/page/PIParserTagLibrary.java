package org.carewebframework.web.page;

import org.carewebframework.web.taglib.TagLibrary;
import org.carewebframework.web.taglib.TagLibraryRegistry;
import org.springframework.util.Assert;
import org.w3c.dom.ProcessingInstruction;

/**
 * Parser for tag library processing instructions.
 */
public class PIParserTagLibrary extends PIParserBase {
    
    public PIParserTagLibrary() {
        super("taglib");
    }
    
    @Override
    public void parse(ProcessingInstruction pi, PageElement element) {
        String uri = getAttribute(pi, "uri", true);
        String prefix = getAttribute(pi, "prefix", true);
        TagLibrary tagLibrary = TagLibraryRegistry.getInstance().get(uri);
        Assert.notNull(tagLibrary, "Tag library not found: " + uri);
        element.addTagLibrary(prefix, tagLibrary);
    }
    
}
