package org.carewebframework.web.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.carewebframework.web.annotation.ComponentDefinition;
import org.carewebframework.web.client.ExecutionContext;
import org.carewebframework.web.component.BaseComponent;
import org.carewebframework.web.component.Page;
import org.carewebframework.web.component.Treenode;
import org.carewebframework.web.component.Treeview;
import org.carewebframework.web.page.PageDefinition;
import org.carewebframework.web.page.PageElement;
import org.carewebframework.web.page.PageUtil;
import org.junit.Test;

public class CWFTest extends MockTest {
    
    private PageDefinition getPageDefinition(String file) {
        String path = "file://" + ExecutionContext.getSession().getServletContext().getRealPath(file);
        return PageUtil.getPageDefinition(path);
    }
    
    private List<BaseComponent> createPage(String file, BaseComponent parent) {
        PageDefinition pagedef = getPageDefinition(file);
        return PageUtil.createPage(pagedef, parent);
    }
    
    @Test
    public void testParser() {
        PageDefinition pagedef = getPageDefinition("test.cwf");
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
    
    private final String[] nodes = { "1.1", "2.1", "3.1", "2.2", "1.2", "1.3", "2.1", "3.1", "2.2" };
    
    @Test
    public void testTreeview() {
        Treeview tv = (Treeview) createPage("treeview.cwf", null).get(0);
        
        // Test the node iterator
        int index = 0;
        
        for (Treenode node : tv) {
            assertEquals(nodes[index++], node.getLabel());
        }
        
        assertEquals(nodes.length, index);
    }
}
