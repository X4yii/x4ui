package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
public class GuiScrollPanel extends GuiPanel {
    protected float scrollY = 0;
    protected float targetScrollY = 0;
    protected int maxScrollY = 0;
    protected float scrollSpeed = 0.2f;
    protected int scrollStep = 22;
    protected GuiPanel contentPanel;
    public GuiScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.flexDirection = com.x4yi.x4ui.client.gui.component.layout.FlexDirection.ABSOLUTE;
        contentPanel = new GuiPanel(0, 0, width, height);
        contentPanel.setFlexDirection(com.x4yi.x4ui.client.gui.component.layout.FlexDirection.VERTICAL);
        super.addChild(contentPanel);
    }
    public GuiPanel getContentPanel() {
        return contentPanel;
    }
    public float getScrollY() { return scrollY; }
    public void setScrollY(float y) { this.scrollY = y; }
    public float getTargetScrollY() { return targetScrollY; }
    public void setTargetScrollY(float y) { this.targetScrollY = y; }
    @Override
    public void addChild(GuiComponent child) {
        contentPanel.addChild(child);
    }
    @Override
    public void removeChild(GuiComponent child) {
        contentPanel.removeChild(child);
    }
    @Override
    public void clearChildren() {
        contentPanel.clearChildren();
    }
    @Override
    public void update() {
        super.update();
        maxScrollY = Math.max(0, contentPanel.getHeight() - this.height);
        if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
        if (targetScrollY < 0) targetScrollY = 0;
        scrollY += (targetScrollY - scrollY) * scrollSpeed;
        if (Math.abs(targetScrollY - scrollY) < 0.5f) {
            scrollY = targetScrollY;
        }
        contentPanel.setY(-(int)scrollY);
    }
    @Override
    protected void drawChildren(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        int scale = com.x4yi.x4ui.client.gui.base.GuiBaseScreen.currentScaleFactor;
        int scissorX = absX * scale;
        int scissorY = mc.displayHeight - (absY + height) * scale;
        int scissorW = width * scale;
        int scissorH = height * scale;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);
        super.drawChildren(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (maxScrollY > 0) {
            int trackX = absX + width - 5;
            int trackY = absY;
            int trackHeight = height;
            drawRect(trackX, trackY, trackX + 2, trackY + trackHeight, 0xFF121215);
            float scrollRatio = scrollY / maxScrollY;
            int thumbHeight = Math.max(20, (int) ((float) height / contentPanel.getHeight() * trackHeight));
            int thumbY = trackY + (int) (scrollRatio * (trackHeight - thumbHeight));
            drawRect(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFF424248);
        }
    }
    @Override
    public boolean handleMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (wheel != 0) {
            int direction = wheel > 0 ? -1 : 1;
            targetScrollY += direction * scrollStep;
            if (targetScrollY < 0) targetScrollY = 0;
            if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
            return true;
        }
        return super.handleMouseScroll(mouseX, mouseY, wheel);
    }
}