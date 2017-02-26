package org.carewebframework.web.test;

import org.carewebframework.web.component.Page;
import org.carewebframework.web.websocket.Session;

public class MockSession extends Session {
    
    public MockSession(MockServletContext servletContext, MockWebSocketSession socket) {
        super(servletContext, socket);
        Page page = Page._create("mockpage");
        _init(page.getId());
    }
    
    @Override
    protected void destroy() {
        super.destroy();
    }
}
