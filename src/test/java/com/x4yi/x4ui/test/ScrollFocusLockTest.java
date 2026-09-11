package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.GuiScrollPanel;
import org.junit.Assert;
import org.junit.Test;
public class ScrollFocusLockTest {
    @Test
    public void testScrollFocusLockingInNestedPanels() {
        GuiPanel root = new GuiPanel(0, 0, 200, 200);
        GuiScrollPanel parentScroll = new GuiScrollPanel(0, 0, 100, 100);
        GuiScrollPanel childScroll = new GuiScrollPanel(0, 0, 50, 100);
        GuiPanel childContent = new GuiPanel(0, 0, 50, 200);
        childScroll.addChild(childContent);
        GuiPanel parentContent = new GuiPanel(0, 0, 100, 300);
        parentScroll.addChild(childScroll);
        parentScroll.addChild(parentContent);
        root.addChild(parentScroll);
        root.update(0.016f);
        Assert.assertTrue("Parent must have scrollable content", parentScroll.getMaxScrollY() > 0);
        Assert.assertTrue("Child must have scrollable content", childScroll.getMaxScrollY() > 0);
        Assert.assertNull("Initial scroll focus must be null", root.getScrollFocusedComponent());
        boolean handled = root.onMouseScroll(80, 80, -1);
        Assert.assertTrue(handled);
        Assert.assertEquals("Parent should have acquired scroll focus", parentScroll, root.getScrollFocusedComponent());
        float initialParentTarget = parentScroll.getTargetScrollY();
        Assert.assertTrue(initialParentTarget > 0);
        Assert.assertEquals(0.0f, childScroll.getTargetScrollY(), 0.001f);
        handled = root.onMouseScroll(25, 25, -1);
        Assert.assertTrue(handled);
        Assert.assertEquals("Parent must retain scroll focus", parentScroll, root.getScrollFocusedComponent());
        Assert.assertTrue("Parent scroll must advance", parentScroll.getTargetScrollY() > initialParentTarget);
        Assert.assertEquals("Child must NOT have scrolled", 0.0f, childScroll.getTargetScrollY(), 0.001f);
        root.update(0.3f);
        Assert.assertNull("Scroll focus lock should expire after debounce timeout", root.getScrollFocusedComponent());
        handled = root.onMouseScroll(25, 25, -1);
        Assert.assertTrue(handled);
        Assert.assertEquals("Child should now acquire scroll focus", childScroll, root.getScrollFocusedComponent());
        Assert.assertTrue("Child should now have scrolled", childScroll.getTargetScrollY() > 0);
    }
    @Test
    public void testCursorExitReleasesLock() {
        GuiPanel root = new GuiPanel(0, 0, 200, 200);
        GuiScrollPanel panel = new GuiScrollPanel(0, 0, 100, 100);
        GuiPanel content = new GuiPanel(0, 0, 100, 300);
        panel.addChild(content);
        root.addChild(panel);
        root.update(0.016f);
        root.onMouseScroll(50, 50, -1);
        Assert.assertEquals(panel, root.getScrollFocusedComponent());
        root.onMouseScroll(150, 150, -1);
        Assert.assertNull("Scroll lock should be released when cursor leaves bounds", root.getScrollFocusedComponent());
    }
    @Test
    public void testScrollbarDragLifecycle() {
        GuiPanel root = new GuiPanel(0, 0, 200, 200);
        GuiScrollPanel panel = new GuiScrollPanel(0, 0, 100, 100);
        GuiPanel content = new GuiPanel(0, 0, 100, 300);
        panel.addChild(content);
        root.addChild(panel);
        root.update(0.016f);
        boolean clicked = root.onMouseClick(96, 50, 0);
        Assert.assertTrue("Clicking scrollbar should be handled", clicked);
        Assert.assertEquals("Scrollbar drag should acquire scroll focus", panel, root.getScrollFocusedComponent());
        root.onMouseDrag(96, 80, 0, 100);
        Assert.assertTrue("Target scroll should have updated during drag", panel.getTargetScrollY() > 0);
        root.onMouseRelease(96, 80, 0);
        Assert.assertNull("Releasing mouse should clear scroll focus", root.getScrollFocusedComponent());
    }
}