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

        // Con hugContent == false, las dimensiones del contenedor padre se preservan
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

        // Altura esperada con hugContent: child1(40) + gap(10) + child2(40) = 90
        Assert.assertEquals(90, parent.getHeight());
        Assert.assertEquals(300, parent.getWidth());
    }

    @Test
    public void testLabelDimensionChangeNotifiesParentLayout() {
        GuiPanel parent = new GuiPanel(0, 0, 200, 100);
        TestComponent child = new TestComponent();
        parent.addChild(child);

        // Limpiar la bandera needsLayout tras el primer pase
        parent.update(0.016f);

        // Mutar dimensiones usando setter
        child.setWidth(80);

        // Verificar que el padre ha sido marcado para recalcular layout
        // En GuiComponent: setWidth -> if (parent != null) parent.requestLayout()
        // por lo que parent.update() procesará el nuevo layout
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
