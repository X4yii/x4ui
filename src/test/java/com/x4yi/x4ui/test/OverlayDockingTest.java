package com.x4yi.x4ui.test;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.overlay.ContainerAnchor;
import com.x4yi.x4ui.client.gui.overlay.IGuiOverlay;
import com.x4yi.x4ui.client.gui.overlay.OverlayContext;
import com.x4yi.x4ui.client.gui.overlay.OverlayDockManager;
import net.minecraft.client.gui.GuiScreen;
import org.junit.Assert;
import org.junit.Test;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class OverlayDockingTest {
    @Test
    public void testContainerAnchorCalculations() {
        int guiLeft = 100;
        int guiTop = 50;
        int xSize = 176;
        int ySize = 166;
        int screenWidth = 480;
        int screenHeight = 270;
        int compWidth = 40;
        int compHeight = 60;
        int offsetX = 4;
        int offsetY = 2;
        int stackOffset = 10;
        Point left = ContainerAnchor.CONTAINER_LEFT.calculate(
                guiLeft, guiTop, xSize, ySize,
                screenWidth, screenHeight, compWidth, compHeight,
                offsetX, offsetY, stackOffset);
        Assert.assertEquals(100 - 40 - 4, left.x);
        Assert.assertEquals(50 + 2 + 10, left.y);
        Point right = ContainerAnchor.CONTAINER_RIGHT.calculate(
                guiLeft, guiTop, xSize, ySize,
                screenWidth, screenHeight, compWidth, compHeight,
                offsetX, offsetY, stackOffset);
        Assert.assertEquals(100 + 176 + 4, right.x);
        Assert.assertEquals(50 + 2 + 10, right.y);
        Point top = ContainerAnchor.CONTAINER_TOP.calculate(
                guiLeft, guiTop, xSize, ySize,
                screenWidth, screenHeight, compWidth, compHeight,
                offsetX, offsetY, stackOffset);
        Assert.assertEquals(100 + 4 + 10, top.x);
        Assert.assertEquals(50 - 60 - 2, top.y);
        Point bottom = ContainerAnchor.CONTAINER_BOTTOM.calculate(
                guiLeft, guiTop, xSize, ySize,
                screenWidth, screenHeight, compWidth, compHeight,
                offsetX, offsetY, stackOffset);
        Assert.assertEquals(100 + 4 + 10, bottom.x);
        Assert.assertEquals(50 + 166 + 2, bottom.y);
    }
    @Test
    public void testOverlayDockManagerStackingAndPriority() {
        GuiPanel panelA = new GuiPanel(0, 0, 30, 40);
        GuiPanel panelB = new GuiPanel(0, 0, 30, 50);
        MockOverlay overlayA = new MockOverlay("modA:tab", 0, ContainerAnchor.CONTAINER_RIGHT, panelA, 4, 0, 5);
        MockOverlay overlayB = new MockOverlay("modB:tab", 10, ContainerAnchor.CONTAINER_RIGHT, panelB, 4, 0, 5);
        GuiScreen dummyScreen = new GuiScreen() {};
        dummyScreen.width = 480;
        dummyScreen.height = 270;
        OverlayContext context = new OverlayContext(dummyScreen);
        List<IGuiOverlay> overlays = Arrays.asList(overlayA, overlayB);
        OverlayDockManager.layout(overlays, context);
        int expectedX = context.getGuiLeft() + context.getXSize() + 4;
        Assert.assertEquals("Overlay B (priority 10) X should match container right + offset", expectedX, panelB.getX());
        Assert.assertEquals("Overlay B (priority 10) Y should start at top + offsetY", context.getGuiTop(), panelB.getY());
        Assert.assertEquals("Overlay A X should match container right + offset", expectedX, panelA.getX());
        Assert.assertEquals("Overlay A Y should be stacked below Overlay B", context.getGuiTop() + 50 + 5, panelA.getY());
    }
    private static class MockOverlay implements IGuiOverlay {
        private final String id;
        private final int priority;
        private final ContainerAnchor anchor;
        private final GuiComponent root;
        private final int offsetX;
        private final int offsetY;
        private final int stackSpacing;
        public MockOverlay(String id, int priority, ContainerAnchor anchor, GuiComponent root, int offsetX, int offsetY, int stackSpacing) {
            this.id = id;
            this.priority = priority;
            this.anchor = anchor;
            this.root = root;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.stackSpacing = stackSpacing;
        }
        @Override public String getId() { return id; }
        @Override public int getPriority() { return priority; }
        @Override public boolean matches(GuiScreen screen) { return true; }
        @Override public void init(GuiScreen screen, OverlayContext context) {}
        @Override public GuiComponent getRootComponent() { return root; }
        @Override public ContainerAnchor getAnchor() { return anchor; }
        @Override public int getOffsetX() { return offsetX; }
        @Override public int getOffsetY() { return offsetY; }
        @Override public int getStackSpacing() { return stackSpacing; }
    }
}