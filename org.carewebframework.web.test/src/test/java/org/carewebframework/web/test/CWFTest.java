package org.carewebframework.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageElement;
import org.carewebframework.web.page.PageUtil;
import org.junit.Test;

public class CWFTest extends MockTest {
    
    @Test
    public void testParser() {
        String path = "file://" + ExecutionContext.getSession().getServletContext().getRealPath("test.cwf");
        PageDefinition pagedef = PageUtil.getPageDefinition(path);
        PageElement pgele = pagedef.getRootElement().getChildren().iterator().next();
        Page page = ExecutionContext.getPage();
        ComponentDefinition cmpdef = pgele.getDefinition();
        assertEquals("page", cmpdef.getTag());
        assertEquals(Page.class, cmpdef.getComponentClass());
        assertEquals("page", pgele.getAttributes().get("name"));
        
        List<BaseComponent> roots = PageUtil.createPage(pagedef, page, null);
        assertEquals(1, roots.size());
        BaseComponent root = roots.get(0);
        assertSame(page, root);
        assertEquals("page", page.getName());
        assertEquals("The Page Title", page.getTitle());
    }
    
}
