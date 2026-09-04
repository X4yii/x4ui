package com.x4yi.x4ui.client.gui.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class GuiComponent {
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected final FontRenderer fontRenderer = mc.fontRenderer;
    protected GuiComponent parent;
    protected final List<GuiComponent> children = new ArrayList<>();
    protected final List<GuiComponent> renderChildren = new ArrayList<>();
    private final List<Runnable> unbinders = new ArrayList<>();
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected com.x4yi.x4ui.client.gui.utils.Insets padding = com.x4yi.x4ui.client.gui.utils.Insets.ZERO;
    protected com.x4yi.x4ui.client.gui.utils.Insets margin = com.x4yi.x4ui.client.gui.utils.Insets.ZERO;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean needsLayout = true;
    protected boolean needsRenderSort = true;
    protected int layer = 0;
    protected String tooltipText = null;
    protected GuiComponent tooltipComponent = null;
    protected com.x4yi.x4ui.client.gui.component.layout.ILayoutManager layoutManager = null;
    protected com.x4yi.x4ui.client.gui.utils.ITheme theme = null;

    protected static final java.util.Comparator<GuiComponent> LAYER_COMPARATOR = java.util.Comparator.comparingInt(GuiComponent::getLayer);
    
    public String getTooltipText() { return tooltipText; }
    public void setTooltip(String tooltipText) { this.tooltipText = tooltipText; }
    public GuiComponent getTooltipComponent() { return tooltipComponent; }
    public void setTooltipComponent(GuiComponent tooltipComponent) { this.tooltipComponent = tooltipComponent; }
    public com.x4yi.x4ui.client.gui.component.layout.ILayoutManager getLayoutManager() { return layoutManager; }
    public void setLayoutManager(com.x4yi.x4ui.client.gui.component.layout.ILayoutManager layoutManager) { this.layoutManager = layoutManager; requestLayout(); }
    public void setTheme(com.x4yi.x4ui.client.gui.utils.ITheme theme) { this.theme = theme; requestRender(); }
    public com.x4yi.x4ui.client.gui.utils.ITheme getTheme() {
        if (this.theme != null) return this.theme;
        if (this.parent != null) return this.parent.getTheme();
        return com.x4yi.x4ui.client.gui.utils.DefaultTheme.INSTANCE;
    }
    public int getLayer() { return layer; }
    public void setLayer(int layer) {
        if (this.layer != layer) {
            this.layer = layer;
            if (parent != null) parent.requestRender();
        }
    }

    public void requestLayout() {
        this.needsLayout = true;
    }

    public void requestRender() {
        this.needsRenderSort = true;
    }

    public void markDirty() {
        requestLayout();
        requestRender();
    }
    
    protected GuiComponent focusedComponent = null;

    public GuiComponent getRoot() {
        GuiComponent current = this;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }

    public void requestFocus() {
        GuiComponent root = getRoot();
        if (root.focusedComponent != this) {
            if (root.focusedComponent != null) {
                root.focusedComponent.onFocusLost();
            }
            root.focusedComponent = this;
            this.onFocusGained();
        }
    }

    public void clearFocus() {
        GuiComponent root = getRoot();
        if (root.focusedComponent == this) {
            this.onFocusLost();
            root.focusedComponent = null;
        }
    }

    public boolean isFocused() {
        return getRoot().focusedComponent == this;
    }

    protected void onFocusGained() {}
    protected void onFocusLost() {}
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
        if (child != null && children.remove(child)) {
            child.destroy();
            child.setParent(null);
            markDirty();
        }
    }
    public void clearChildren() {
        for (GuiComponent child : children) {
            child.destroy();
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
    public void tick(float deltaTime) {
        if (!visible) return;
        if (needsLayout && layoutManager != null) {
            layoutManager.layout(this, children);
            needsLayout = false;
        }
        for (GuiComponent child : children) {
            child.tick(deltaTime);
        }
    }
    public void destroy() {
        for (Runnable unbinder : unbinders) {
            unbinder.run();
        }
        unbinders.clear();
        for (GuiComponent child : children) {
            child.destroy();
        }
    }
    public <T> void bindState(com.x4yi.x4ui.common.State<T> state, java.util.function.Consumer<T> onChange) {
        java.util.function.Consumer<T> listener = value -> {
            if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                onChange.accept(value);
            } else {
                Minecraft.getMinecraft().addScheduledTask(() -> onChange.accept(value));
            }
        };
        state.addListener(listener);
        unbinders.add(() -> state.removeListener(listener));
        listener.accept(state.get());
    }
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        renderSelf(mouseX, mouseY, partialTicks);
        renderChildren(mouseX, mouseY, partialTicks);
    }
    protected abstract void renderSelf(int mouseX, int mouseY, float partialTicks);
    protected void renderChildren(int mouseX, int mouseY, float partialTicks) {
        if (needsRenderSort) {
            renderChildren.clear();
            renderChildren.addAll(children);
            renderChildren.sort(LAYER_COMPARATOR);
            needsRenderSort = false;
        }
        for (GuiComponent child : renderChildren) {
            child.render(mouseX, mouseY, partialTicks);
        }
    }
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled) return false;
        
        if (this.parent == null) {
            GuiComponent hovered = getHoveredComponent(mouseX, mouseY);
            if (this.focusedComponent != null && hovered != this.focusedComponent) {
                this.focusedComponent.clearFocus();
            }
        }

        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            if (renderChildren.get(i).onMouseClick(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return false;
    }
    public boolean onMouseRelease(int mouseX, int mouseY, int state) {
        if (!visible || !enabled) return false;
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            if (renderChildren.get(i).onMouseRelease(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }
    public boolean onMouseDrag(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!visible || !enabled) return false;
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            if (renderChildren.get(i).onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }
    public boolean onKeyPress(char typedChar, int keyCode) {
        if (!visible || !enabled) return false;

        if (this.parent == null && this.focusedComponent != null) {
            if (this.focusedComponent.onKeyPress(typedChar, keyCode)) {
                return true;
            }
        }

        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            GuiComponent child = renderChildren.get(i);
            if (this.parent == null && child == this.focusedComponent) continue; 
            
            if (child.onKeyPress(typedChar, keyCode)) {
                return true;
            }
        }
        return false;
    }
    public boolean onMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!visible || !enabled) return false;
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            if (renderChildren.get(i).onMouseScroll(mouseX, mouseY, wheel)) {
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
    public GuiComponent getHoveredComponent(int mouseX, int mouseY) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return null;
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            GuiComponent hovered = renderChildren.get(i).getHoveredComponent(mouseX, mouseY);
            if (hovered != null) return hovered;
        }
        if (tooltipText != null || tooltipComponent != null) {
            return this;
        }
        return null;
    }
    protected boolean isMouseOver(int mouseX, int mouseY) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        return mouseX >= absX && mouseX <= absX + width &&
               mouseY >= absY && mouseY <= absY + height;
    }
    public static void drawRect(int left, int top, int right, int bottom, int color) {
        net.minecraft.client.gui.Gui.drawRect(left, top, right, bottom, color);
    }
    public static void drawBorder(int left, int top, int right, int bottom, int color) {
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
    public com.x4yi.x4ui.client.gui.utils.Insets getPadding() { return padding; }
    public com.x4yi.x4ui.client.gui.utils.Insets getMargin() { return margin; }
    public void setX(int x) { if (this.x != x) { this.x = x; if (parent != null) parent.requestLayout(); } }
    public void setY(int y) { if (this.y != y) { this.y = y; if (parent != null) parent.requestLayout(); } }
    public void setWidth(int width) { if (this.width != width) { this.width = width; requestLayout(); if (parent != null) parent.requestLayout(); } }
    public void setHeight(int height) { if (this.height != height) { this.height = height; requestLayout(); if (parent != null) parent.requestLayout(); } }
    public void setPadding(com.x4yi.x4ui.client.gui.utils.Insets padding) { this.padding = padding; requestLayout(); }
    public void setMargin(com.x4yi.x4ui.client.gui.utils.Insets margin) { this.margin = margin; if (parent != null) parent.requestLayout(); }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { if (this.visible != visible) { this.visible = visible; if (parent != null) parent.requestLayout(); } }
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