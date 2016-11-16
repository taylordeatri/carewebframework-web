package org.carewebframework.web.core;

import static org.junit.Assert.assertEquals;

import org.carewebframework.web.event.KeyCode;
import org.junit.Test;

public class Tests {
    
    @Test
    public void keyCodeTest() {
        assertEquals(KeyCode.VK_BACK_SPACE, KeyCode.fromCode(8));
        assertEquals(KeyCode.VK_ASTERISK, KeyCode.fromString("ASTERISK"));
        assertEquals(KeyCode.normalizeKeyCapture("^A ~F1 ^@~@^!1"), "^#65 ~#112 ^@~!#49");
    }
    
}
