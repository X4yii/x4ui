package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public abstract class GuiComponent {
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final FontRenderer fontRenderer = mc.fontRenderer;
    protected GuiComponent parent;
    protected final List<GuiComponent> children = new ArrayList<>();
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int padding = 0;
    protected int margin = 0;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean needsLayout = true;
    public void markDirty() {
        this.needsLayout = true;
        if (parent != null) {
            parent.markDirty();
        }
    }
    public GuiComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void addChild(GuiComponent child) {
        child.setParent(this);
        children.add(child);
        markDirty();
    }
    public void removeChild(GuiComponent child) {
        children.remove(child);
        child.setParent(null);
        markDirty();
    }
    public void clearChildren() {
        for (GuiComponent child : children) {
            child.setParent(null);
        }
        children.clear();
        markDirty();
    }
    public List<GuiComponent> getChildren() {
        return children;
    }
    public void setParent(GuiComponent parent) {
        this.parent = parent;
    }
    public GuiComponent getParent() {
        return parent;
    }
    public int getAbsoluteX() {
        return parent == null ? x : parent.getAbsoluteX() + x;
    }
    public int getAbsoluteY() {
        return parent == null ? y : parent.getAbsoluteY() + y;
    }
    public void update() {
        if (!visible) return;
        for (GuiComponent child : children) {
            child.update();
        }
    }
    public void drawComponent(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        drawSelf(mouseX, mouseY, partialTicks);
        drawChildren(mouseX, mouseY, partialTicks);
    }
    protected abstract void drawSelf(int mouseX, int mouseY, float partialTicks);
    protected void drawChildren(int mouseX, int mouseY, float partialTicks) {
        for (GuiComponent child : children) {
            child.drawComponent(mouseX, mouseY, partialTicks);
        }
    }
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }
    public boolean mouseReleased(int mouseX, int mouseY, int state) {
        if (!visible || !enabled) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseReleased(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }
    public boolean mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!visible || !enabled) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }
    public boolean keyTyped(char typedChar, int keyCode) {
        if (!visible || !enabled) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).keyTyped(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }
    public boolean handleMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!visible || !enabled) return false;
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).handleMouseScroll(mouseX, mouseY, wheel)) {
                return true;
            }
        }
        return false;
    }
    public void handleMouseInput() throws IOException {
        for (GuiComponent child : children) {
            child.handleMouseInput();
        }
    }
    protected boolean isMouseOver(int mouseX, int mouseY) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        return mouseX >= absX && mouseX <= absX + width &&
               mouseY >= absY && mouseY <= absY + height;
    }
    protected void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
    protected void drawBorder(int left, int top, int right, int bottom, int color) {
        drawRect(left, top, right, top + 1, color);
        drawRect(left, bottom - 1, right, bottom, color);
        drawRect(left, top + 1, left + 1, bottom - 1, color);
        drawRect(right - 1, top + 1, right, bottom - 1, color);
    }
    protected void drawCenteredString(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x - fontRenderer.getStringWidth(text) / 2, y, color);
    }
    protected void drawString(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x, y, color);
    }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getPadding() { return padding; }
    public int getMargin() { return margin; }
    public void setX(int x) { if (this.x != x) { this.x = x; markDirty(); } }
    public void setY(int y) { if (this.y != y) { this.y = y; markDirty(); } }
    public void setWidth(int width) { if (this.width != width) { this.width = width; markDirty(); } }
    public void setHeight(int height) { if (this.height != height) { this.height = height; markDirty(); } }
    public void setPadding(int padding) { if (this.padding != padding) { this.padding = padding; markDirty(); } }
    public void setMargin(int margin) { if (this.margin != margin) { this.margin = margin; markDirty(); } }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { if (this.visible != visible) { this.visible = visible; markDirty(); } }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    protected int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    protected float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}