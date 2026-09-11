package com.x4yi.x4ui.test;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.utils.Insets;
import com.x4yi.x4ui.common.State;
import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class ComponentHierarchyLifecycleTest {
    private static class TestComponent extends GuiComponent {
        boolean attached = false;
        boolean detached = false;
        float totalDelta = 0.0f;
        int renderCount = 0;
        TestComponent(int x, int y, int width, int height) {
            super(x, y, width, height);
        }
        @Override
        protected void onAttached(GuiComponent parent) {
            this.attached = true;
            this.detached = false;
        }
        @Override
        protected void onDetached() {
            this.detached = true;
            this.attached = false;
        }
        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            this.totalDelta += deltaTime;
        }
        @Override
        protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
            renderCount++;
        }
    }
    @Test
    public void testLifecycleAttachmentAndDetachment() {
        GuiPanel root = new GuiPanel(0, 0, 200, 200);
        TestComponent child = new TestComponent(10, 10, 50, 50);
        Assert.assertFalse(child.attached);
        Assert.assertFalse(child.detached);
        root.addChild(child);
        Assert.assertTrue(child.attached);
        Assert.assertFalse(child.detached);
        Assert.assertEquals(root, child.getParent());
        root.removeChild(child);
        Assert.assertFalse(child.attached);
        Assert.assertTrue(child.detached);
        Assert.assertNull(child.getParent());
    }
    @Test
    public void testUpdatePropagation() {
        GuiPanel root = new GuiPanel(0, 0, 100, 100);
        TestComponent child = new TestComponent(0, 0, 10, 10);
        root.addChild(child);
        root.update(0.016f);
        root.update(0.016f);
        Assert.assertEquals(0.032f, child.totalDelta, 0.0001f);
    }
    @Test
    public void testFluentSetters() {
        TestComponent comp = new TestComponent(0, 0, 10, 10);
        comp.withPosition(25, 30)
            .withSize(120, 80)
            .withPadding(new Insets(4, 6, 4, 6))
            .withMargin(2, 2, 2, 2)
            .withTooltip("Test Tooltip")
            .withVisible(true)
            .withEnabled(false);
        Assert.assertEquals(25, comp.getX());
        Assert.assertEquals(30, comp.getY());
        Assert.assertEquals(120, comp.getWidth());
        Assert.assertEquals(80, comp.getHeight());
        Assert.assertEquals(4, comp.getPadding().top);
        Assert.assertEquals(6, comp.getPadding().left);
        Assert.assertEquals(2, comp.getMargin().top);
        Assert.assertEquals("Test Tooltip", comp.getTooltipText());
        Assert.assertTrue(comp.isVisible());
        Assert.assertFalse(comp.isEnabled());
    }
    @Test
    public void testStateBindingAndDestroyUnbind() {
        TestComponent comp = new TestComponent(0, 0, 10, 10);
        State<String> nameState = new State<>("Initial");
        AtomicInteger listenerInvocations = new AtomicInteger(0);
        comp.bindState(nameState, val -> listenerInvocations.incrementAndGet());
        Assert.assertEquals(1, listenerInvocations.get());
        nameState.set("Second");
        Assert.assertEquals(2, listenerInvocations.get());
        comp.destroy();
        nameState.set("Third");
        Assert.assertEquals(2, listenerInvocations.get());
    }
}