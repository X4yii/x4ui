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
        this.setFlexDirection(com.x4yi.x4ui.client.gui.component.layout.FlexDirection.ABSOLUTE);
        contentPanel = new GuiPanel(0, 0, width, height) {
            @Override
            protected void renderChildren(int mouseX, int mouseY, float partialTicks) {
                if (needsRenderSort) {
                    renderChildren.clear();
                    renderChildren.addAll(children);
                    renderChildren.sort(LAYER_COMPARATOR);
                    needsRenderSort = false;
                }
                int scrollViewY = (int) scrollY;
                int viewHeight = GuiScrollPanel.this.height;
                for (GuiComponent child : renderChildren) {
                    int childBottom = child.getY() - scrollViewY + child.getHeight() + child.getMargin().bottom;
                    int childTop = child.getY() - scrollViewY - child.getMargin().top;
                    if (childBottom > 0 && childTop < viewHeight) {
                        child.render(mouseX, mouseY, partialTicks);
                    }
                }
            }
            @Override
            public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
                if (!visible || !enabled) return false;
                int viewHeight = GuiScrollPanel.this.height;
                int scrollViewY = (int) scrollY;
                for (int i = renderChildren.size() - 1; i >= 0; i--) {
                    GuiComponent child = renderChildren.get(i);
                    int childBottom = child.getY() - scrollViewY + child.getHeight() + child.getMargin().bottom;
                    int childTop = child.getY() - scrollViewY - child.getMargin().top;
                    if (childBottom > 0 && childTop < viewHeight) {
                        if (child.onMouseClick(mouseX, mouseY, mouseButton)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
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
    public int getMaxScrollY() { return maxScrollY; }
    public void scrollToBottom() { this.targetScrollY = this.maxScrollY; }
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
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        maxScrollY = Math.max(0, contentPanel.getHeight() - this.height);
        if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
        if (targetScrollY < 0) targetScrollY = 0;
        float dt = com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime;
        scrollY += (targetScrollY - scrollY) * (scrollSpeed * 60f * dt);
        if (Math.abs(targetScrollY - scrollY) < 0.5f) {
            scrollY = targetScrollY;
        }
        contentPanel.setY(-(int)scrollY);
    }
    @Override
    protected void renderChildren(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        int scale = com.x4yi.x4ui.client.gui.base.GuiBaseScreen.currentScaleFactor;
        int scissorX = absX * scale;
        int scissorY = mc.displayHeight - (absY + height) * scale;
        int scissorW = width * scale;
        int scissorH = height * scale;
        com.x4yi.x4ui.client.gui.utils.ScissorHelper.pushScissor(scissorX, scissorY, scissorW, scissorH);
        super.renderChildren(mouseX, mouseY, partialTicks);
        com.x4yi.x4ui.client.gui.utils.ScissorHelper.popScissor();
        if (maxScrollY > 0) {
            com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
            int trackX = absX + width - 5;
            int trackY = absY;
            int trackHeight = height;
            drawRect(trackX, trackY, trackX + 2, trackY + trackHeight, theme.getScrollbarTrackColor());
            float scrollRatio = scrollY / maxScrollY;
            int thumbHeight = Math.max(20, (int) ((float) height / contentPanel.getHeight() * trackHeight));
            int thumbY = trackY + (int) (scrollRatio * (trackHeight - thumbHeight));
            drawRect(trackX, thumbY, trackX + 2, thumbY + thumbHeight, theme.getScrollbarThumbColor());
        }
    }
    @Override
    public boolean onMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (super.onMouseScroll(mouseX, mouseY, wheel)) {
            return true;
        }
        if (wheel != 0 && maxScrollY > 0) {
            int direction = wheel > 0 ? -1 : 1;
            targetScrollY += direction * scrollStep;
            if (targetScrollY < 0) targetScrollY = 0;
            if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
            return true;
        }
        return false;
    }
}