package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.impl.client.gui.GLGraphics;
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
    protected final FontRenderer fontRenderer = mc != null ? mc.fontRenderer : null;
    protected GuiComponent parent;
    protected final List<GuiComponent> children = new ArrayList<>();
    protected final List<GuiComponent> renderChildren = new ArrayList<>();
    private final List<Runnable> unbinders = new ArrayList<>();
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected float percentX = -1.0f;
    protected float percentY = -1.0f;
    protected float percentWidth = -1.0f;
    protected float percentHeight = -1.0f;
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
    protected com.x4yi.x4ui.api.client.font.IFont font = null;
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
        return com.x4yi.x4ui.client.gui.utils.ThemeRegistry.getCurrentTheme();
    }
    public GuiComponent withFont(com.x4yi.x4ui.api.client.font.IFont font) {
        this.font = font;
        requestRender();
        return this;
    }
    public GuiComponent withFont(String fontName) {
        this.font = com.x4yi.x4ui.impl.client.font.FontRegistry.INSTANCE.get(fontName);
        requestRender();
        return this;
    }
    public void setFont(com.x4yi.x4ui.api.client.font.IFont font) {
        this.font = font;
        requestRender();
    }
    public com.x4yi.x4ui.api.client.font.IFont getFont() {
        if (this.font != null) return this.font;
        if (this.parent != null) return this.parent.getFont();
        return getTheme() != null ? getTheme().getFont() : com.x4yi.x4ui.impl.client.font.VanillaFont.INSTANCE;
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
    protected GuiComponent scrollFocusedComponent = null;
    protected float scrollFocusTimer = 0.0f;
    public static final float DEFAULT_SCROLL_FOCUS_TIMEOUT = 0.25f;
    public void requestScrollFocus(GuiComponent component) {
        GuiComponent root = getRoot();
        root.scrollFocusedComponent = component;
        root.scrollFocusTimer = DEFAULT_SCROLL_FOCUS_TIMEOUT;
    }
    public void clearScrollFocus() {
        GuiComponent root = getRoot();
        root.scrollFocusedComponent = null;
        root.scrollFocusTimer = 0.0f;
    }
    public GuiComponent getScrollFocusedComponent() {
        return getRoot().scrollFocusedComponent;
    }
    public boolean hasScrollFocus() {
        return getRoot().scrollFocusedComponent == this;
    }
    public GuiComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    protected void onAttached(GuiComponent parent) {}
    protected void onDetached() {}
    public void addChild(GuiComponent child) {
        child.setParent(this);
        children.add(child);
        child.onAttached(this);
        markDirty();
    }
    public void removeChild(GuiComponent child) {
        if (child != null && children.remove(child)) {
            child.destroy();
            child.onDetached();
            child.setParent(null);
            markDirty();
        }
    }
    public void clearChildren() {
        for (GuiComponent child : children) {
            child.destroy();
            child.onDetached();
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
    public void update(float deltaTime) {
        if (!visible) return;
        if (parent != null) {
            com.x4yi.x4ui.client.gui.utils.Insets parentPad = parent.getPadding();
            if (percentWidth >= 0.0f) {
                int availW = Math.max(0, parent.getWidth() - parentPad.left - parentPad.right - margin.left - margin.right);
                int pw = Math.max(0, (int) (availW * percentWidth));
                if (this.width != pw) {
                    setWidth(pw);
                }
            }
            if (percentHeight >= 0.0f) {
                int availH = Math.max(0, parent.getHeight() - parentPad.top - parentPad.bottom - margin.top - margin.bottom);
                int ph = Math.max(0, (int) (availH * percentHeight));
                if (this.height != ph) {
                    setHeight(ph);
                }
            }
            if (percentX >= 0.0f) {
                int px = (int) (parent.getWidth() * percentX);
                if (this.x != px) {
                    setX(px);
                }
            }
            if (percentY >= 0.0f) {
                int py = (int) (parent.getHeight() * percentY);
                if (this.y != py) {
                    setY(py);
                }
            }
        }
        if (this.parent == null && this.scrollFocusedComponent != null) {
            this.scrollFocusTimer -= deltaTime;
            if (this.scrollFocusTimer <= 0.0f) {
                this.scrollFocusedComponent = null;
                this.scrollFocusTimer = 0.0f;
            }
        }
        for (GuiComponent child : children) {
            child.update(deltaTime);
        }
        if (needsLayout && layoutManager != null) {
            layoutManager.layout(this, children);
            needsLayout = false;
        }
    }
    public void destroy() {
        onDestroy();
        com.x4yi.x4ui.client.gui.animation.GuiAnimator.cancel(this);
        for (Runnable unbinder : unbinders) {
            unbinder.run();
        }
        unbinders.clear();
        for (GuiComponent child : children) {
            child.destroy();
        }
    }
    protected void onDestroy() {}
    public void addUnbinder(Runnable unbinder) {
        if (unbinder != null) {
            this.unbinders.add(unbinder);
        }
    }
    public <T, R> com.x4yi.x4ui.common.State<R> mapState(com.x4yi.x4ui.common.State<T> source, java.util.function.Function<T, R> mapper) {
        com.x4yi.x4ui.common.State<R> mapped = source.map(mapper);
        addUnbinder(mapped::dispose);
        return mapped;
    }
    public <T> void bindState(com.x4yi.x4ui.common.State<T> state, java.util.function.Consumer<T> onChange) {
        java.util.function.Consumer<T> listener = value -> {
            Minecraft client = Minecraft.getMinecraft();
            if (client == null || client.isCallingFromMinecraftThread()) {
                onChange.accept(value);
            } else {
                client.addScheduledTask(() -> onChange.accept(value));
            }
        };
        state.addListener(listener);
        unbinders.add(() -> state.removeListener(listener));
        listener.accept(state.get());
    }
    public void render(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        if (gfx.isClipped(getAbsoluteX(), getAbsoluteY(), width, height)) return;
        com.x4yi.x4ui.api.client.font.IFont prevFont = null;
        if (this.font != null) {
            prevFont = gfx.getFont();
            gfx.setFont(this.font);
        }
        try {
            renderSelf(gfx, mouseX, mouseY, partialTicks);
            renderChildren(gfx, mouseX, mouseY, partialTicks);
        } finally {
            if (this.font != null) {
                gfx.setFont(prevFont);
            }
        }
    }
    protected abstract void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks);
    protected void ensureRenderChildren() {
        if (needsRenderSort) {
            renderChildren.clear();
            renderChildren.addAll(children);
            renderChildren.sort(LAYER_COMPARATOR);
            needsRenderSort = false;
        }
    }
    protected void renderChildren(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        ensureRenderChildren();
        for (GuiComponent child : renderChildren) {
            child.render(gfx, mouseX, mouseY, partialTicks);
        }
    }
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled) return false;
        ensureRenderChildren();
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
        ensureRenderChildren();
        if (this.parent == null && this.scrollFocusedComponent != null) {
            if (this.scrollFocusedComponent.onMouseRelease(mouseX, mouseY, state)) {
                return true;
            }
        }
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            if (renderChildren.get(i).onMouseRelease(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }
    public boolean onMouseDrag(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!visible || !enabled) return false;
        ensureRenderChildren();
        if (this.parent == null && this.scrollFocusedComponent != null) {
            if (this.scrollFocusedComponent.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            if (renderChildren.get(i).onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return false;
    }
    public boolean onKeyPress(char typedChar, int keyCode) {
        if (!visible || !enabled) return false;
        ensureRenderChildren();
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
        ensureRenderChildren();
        if (this.parent == null && this.scrollFocusedComponent != null) {
            if (this.scrollFocusedComponent.isVisible() && this.scrollFocusedComponent.isEnabled()
                    && this.scrollFocusedComponent.contains(mouseX, mouseY)) {
                this.scrollFocusTimer = DEFAULT_SCROLL_FOCUS_TIMEOUT;
                if (this.scrollFocusedComponent.onMouseScroll(mouseX, mouseY, wheel)) {
                    return true;
                }
            } else {
                this.scrollFocusedComponent = null;
                this.scrollFocusTimer = 0.0f;
            }
        }
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            GuiComponent child = renderChildren.get(i);
            if (this.parent == null && child == this.scrollFocusedComponent) continue;
            if (child.onMouseScroll(mouseX, mouseY, wheel)) {
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
        ensureRenderChildren();
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            GuiComponent hovered = renderChildren.get(i).getHoveredComponent(mouseX, mouseY);
            if (hovered != null) return hovered;
        }
        return this;
    }
    public GuiComponent getTooltipSource(int mouseX, int mouseY) {
        GuiComponent current = getHoveredComponent(mouseX, mouseY);
        while (current != null && current.getTooltipText() == null && current.getTooltipComponent() == null) {
            current = current.getParent();
        }
        return current;
    }
    public boolean contains(int mouseX, int mouseY) {
        return isMouseOver(mouseX, mouseY);
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
        getFont().draw(text, x - getFont().getWidth(text) / 2.0f, y, color, false);
    }
    protected void drawString(String text, int x, int y, int color) {
        getFont().draw(text, x, y, color, false);
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
    public boolean isEffectivelyVisible() {
        GuiComponent current = this;
        while (current != null) {
            if (!current.visible) return false;
            current = current.parent;
        }
        return true;
    }
    protected void onEffectiveVisibilityChanged(boolean effectivelyVisible) {
        for (GuiComponent child : children) {
            if (child.visible) {
                child.onEffectiveVisibilityChanged(effectivelyVisible);
            }
        }
    }
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;
            if (parent != null) parent.requestLayout();
            onEffectiveVisibilityChanged(isEffectivelyVisible());
        }
    }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public GuiComponent withPosition(int x, int y) { setX(x); setY(y); return this; }
    public GuiComponent withSize(int width, int height) { setWidth(width); setHeight(height); return this; }
    public GuiComponent withPadding(com.x4yi.x4ui.client.gui.utils.Insets padding) { setPadding(padding); return this; }
    public GuiComponent withPadding(int top, int right, int bottom, int left) { return withPadding(new com.x4yi.x4ui.client.gui.utils.Insets(top, right, bottom, left)); }
    public GuiComponent withMargin(com.x4yi.x4ui.client.gui.utils.Insets margin) { setMargin(margin); return this; }
    public GuiComponent withMargin(int top, int right, int bottom, int left) { return withMargin(new com.x4yi.x4ui.client.gui.utils.Insets(top, right, bottom, left)); }
    public float getPercentX() { return percentX; }
    public float getPercentY() { return percentY; }
    public float getPercentWidth() { return percentWidth; }
    public float getPercentHeight() { return percentHeight; }
    public void setPercentX(float percentX) { this.percentX = percentX; if (parent != null) parent.requestLayout(); }
    public void setPercentY(float percentY) { this.percentY = percentY; if (parent != null) parent.requestLayout(); }
    public void setPercentWidth(float percentWidth) { this.percentWidth = percentWidth; requestLayout(); if (parent != null) parent.requestLayout(); }
    public void setPercentHeight(float percentHeight) { this.percentHeight = percentHeight; requestLayout(); if (parent != null) parent.requestLayout(); }
    public GuiComponent withPercentPosition(float percentX, float percentY) { setPercentX(percentX); setPercentY(percentY); return this; }
    public GuiComponent withPercentWidth(float percentWidth) { setPercentWidth(percentWidth); return this; }
    public GuiComponent withPercentHeight(float percentHeight) { setPercentHeight(percentHeight); return this; }
    public GuiComponent withPercentSize(float percentWidth, float percentHeight) { setPercentWidth(percentWidth); setPercentHeight(percentHeight); return this; }
    public GuiComponent withVisible(boolean visible) { setVisible(visible); return this; }
    public GuiComponent withEnabled(boolean enabled) { setEnabled(enabled); return this; }
    public GuiComponent withTooltip(String tooltipText) { setTooltip(tooltipText); return this; }
    public GuiComponent withTooltip(GuiComponent tooltipComponent) { setTooltipComponent(tooltipComponent); return this; }
    public GuiComponent withTheme(com.x4yi.x4ui.client.gui.utils.ITheme theme) { setTheme(theme); return this; }
    public GuiComponent withLayer(int layer) { setLayer(layer); return this; }
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