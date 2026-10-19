package com.x4yi.x4ui.test;

import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.component.layout.FlexLayout;
import org.junit.Assert;
import org.junit.Test;

public class FlexLayoutHugContentTest {

    private static class DummyBlock extends GuiComponent {
        DummyBlock(int w, int h) {
            super(0, 0, w, h);
        }
        @Override
        protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {}
    }

    @Test
    public void testNonDestructiveByDefault() {
        GuiPanel parent = new GuiPanel(0, 0, 300, 200);
        FlexLayout layout = new FlexLayout(FlexDirection.VERTICAL, 10, false);
        Assert.assertFalse("hugContent should be false by default", layout.isHugContent());
        parent.setLayoutManager(layout);

        DummyBlock child1 = new DummyBlock(50, 40);
        DummyBlock child2 = new DummyBlock(50, 40);
        parent.addChild(child1);
        parent.addChild(child2);

        parent.update(0.016f);

        Assert.assertEquals(300, parent.getWidth());
        Assert.assertEquals(200, parent.getHeight());
    }

    @Test
    public void testHugContentTrueResizesParent() {
        GuiPanel parent = new GuiPanel(0, 0, 300, 200);
        FlexLayout layout = new FlexLayout(FlexDirection.VERTICAL, 10, false).withHugContent(true);
        Assert.assertTrue(layout.isHugContent());
        parent.setLayoutManager(layout);

        DummyBlock child1 = new DummyBlock(50, 40);
        DummyBlock child2 = new DummyBlock(50, 40);
        parent.addChild(child1);
        parent.addChild(child2);

        parent.update(0.016f);

        Assert.assertEquals(90, parent.getHeight());
        Assert.assertEquals(300, parent.getWidth());
    }

    @Test
    public void testLabelDimensionChangeNotifiesParentLayout() {
        GuiPanel parent = new GuiPanel(0, 0, 200, 100);
        TestComponent child = new TestComponent();
        parent.addChild(child);

        parent.update(0.016f);

        child.setWidth(80);

        DummyBlock dummy = new DummyBlock(10, 10);
        parent.addChild(dummy);
        Assert.assertNotNull(parent);
    }

    private static class TestComponent extends GuiComponent {
        TestComponent() {
            super(0, 0, 50, 20);
        }
        @Override
        protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {}
    }
}
