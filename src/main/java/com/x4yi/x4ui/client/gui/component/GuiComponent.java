package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
@SideOnly(Side.CLIENT)
public abstract class GuiComponent {
    protected final Minecraft mc = Minecraft.getMinecraft();
    protected GuiComponent parent;
    protected final List<GuiComponent> children = new ArrayList<>();
    protected final List<GuiComponent> renderChildren = new ArrayList<>();
    private final List<Runnable> unbinders = new ArrayList<>();
    protected int x;
    protected int y;
    private int cachedAbsX;
    private int cachedAbsY;
    private boolean absDirty = true;
    protected int width;
    protected int height;
    protected int baseWidth;
    protected int baseHeight;
    protected int minWidth = 0;
    protected int maxWidth = Integer.MAX_VALUE;
    protected int minHeight = 0;
    protected int maxHeight = Integer.MAX_VALUE;
    protected float flexGrow = 0.0f;
    protected float flexShrink = 0.0f;
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
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
        this.baseWidth = this.width;
        this.baseHeight = this.height;
    }
    protected void onAttached(GuiComponent parent) {}
    protected void onDetached() {}
    public <T extends GuiComponent> T add(T child) {
        if (child != null) {
            addChild(child);
        }
        return child;
    }
    public <T extends GuiComponent> T add(T child, Consumer<T> config) {
        if (child != null) {
            if (config != null) {
                config.accept(child);
            }
            addChild(child);
        }
        return child;
    }
    public void addChild(GuiComponent child) {
        if (child == null) return;
        child.setParent(this);
        children.add(child);
        child.onAttached(this);
        markDirty();
    }
    private void clearDanglingFocus(GuiComponent removed) {
        GuiComponent root = getRoot();
        if (root.focusedComponent == removed || isDescendant(removed, root.focusedComponent)) {
            root.focusedComponent = null;
        }
        if (root.scrollFocusedComponent == removed || isDescendant(removed, root.scrollFocusedComponent)) {
            root.scrollFocusedComponent = null;
            root.scrollFocusTimer = 0.0f;
        }
    }
    private static boolean isDescendant(GuiComponent ancestor, GuiComponent candidate) {
        GuiComponent cur = candidate;
        while (cur != null) {
            if (cur == ancestor) return true;
            cur = cur.parent;
        }
        return false;
    }
    public void removeChild(GuiComponent child) {
        if (child != null && children.remove(child)) {
            clearDanglingFocus(child);
            child.destroy();
            child.onDetached();
            child.setParent(null);
            markDirty();
        }
    }
    public void clearChildren() {
        for (GuiComponent child : children) {
            clearDanglingFocus(child);
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
        invalidateAbs();
    }
    public GuiComponent getParent() {
        return parent;
    }
    protected void invalidateAbs() {
        if (!absDirty) {
            absDirty = true;
            for (GuiComponent child : children) {
                child.invalidateAbs();
            }
        }
    }
    public int getAbsoluteX() {
        if (absDirty) {
            cachedAbsX = parent == null ? x : parent.getAbsoluteX() + x;
            cachedAbsY = parent == null ? y : parent.getAbsoluteY() + y;
            absDirty = false;
        }
        return cachedAbsX;
    }
    public int getAbsoluteY() {
        if (absDirty) {
            cachedAbsX = parent == null ? x : parent.getAbsoluteX() + x;
            cachedAbsY = parent == null ? y : parent.getAbsoluteY() + y;
            absDirty = false;
        }
        return cachedAbsY;
    }
    public void update(float deltaTime) {
        if (!visible) return;
        if (parent != null) {
            com.x4yi.x4ui.client.gui.utils.Insets parentPad = parent.getPadding();
            if (percentWidth >= 0.0f) {
                int availW = Math.max(0, parent.getWidth() - parentPad.left - parentPad.right - margin.left - margin.right);
                int pw = clampWidth(Math.max(0, (int) (availW * percentWidth)));
                boolean flexControlled = (flexGrow > 0.0f || flexShrink > 0.0f);
                if (this.baseWidth != pw) {
                    this.baseWidth = pw;
                    if (!flexControlled && this.width != pw) {
                        applyLayoutWidth(pw);
                    } else if (flexControlled) {
                        parent.requestLayout();
                    }
                } else if (!flexControlled && this.width != pw) {
                    applyLayoutWidth(pw);
                }
            }
            if (percentHeight >= 0.0f) {
                int availH = Math.max(0, parent.getHeight() - parentPad.top - parentPad.bottom - margin.top - margin.bottom);
                int ph = clampHeight(Math.max(0, (int) (availH * percentHeight)));
                boolean flexControlled = (flexGrow > 0.0f || flexShrink > 0.0f);
                if (this.baseHeight != ph) {
                    this.baseHeight = ph;
                    if (!flexControlled && this.height != ph) {
                        applyLayoutHeight(ph);
                    } else if (flexControlled) {
                        parent.requestLayout();
                    }
                } else if (!flexControlled && this.height != ph) {
                    applyLayoutHeight(ph);
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
        if (needsLayout && layoutManager != null) {
            layoutManager.layout(this, children);
            needsLayout = false;
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
    protected boolean isClippedBy(IGraphics gfx) {
        return gfx.isClipped(getAbsoluteX(), getAbsoluteY(), width, height);
    }
    public void render(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        if (isClippedBy(gfx)) return;
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
        GuiComponent scrollFocused = (this.parent == null) ? this.scrollFocusedComponent : null;
        if (scrollFocused != null) {
            if (scrollFocused.onMouseRelease(mouseX, mouseY, state)) {
                return true;
            }
        }
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            GuiComponent child = renderChildren.get(i);
            if (child == scrollFocused) continue;
            if (child.onMouseRelease(mouseX, mouseY, state)) {
                return true;
            }
        }
        return false;
    }
    public boolean onMouseDrag(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!visible || !enabled) return false;
        ensureRenderChildren();
        GuiComponent scrollFocused = (this.parent == null) ? this.scrollFocusedComponent : null;
        if (scrollFocused != null) {
            if (scrollFocused.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        for (int i = renderChildren.size() - 1; i >= 0; i--) {
            GuiComponent child = renderChildren.get(i);
            if (child == scrollFocused) continue;
            if (child.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) {
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
    protected void onBoundsChanged() {}
    public int clampWidth(int w) {
        return Math.max(minWidth, Math.min(maxWidth, Math.max(0, w)));
    }
    public int clampHeight(int h) {
        return Math.max(minHeight, Math.min(maxHeight, Math.max(0, h)));
    }
    public void applyLayoutWidth(int w) {
        int clamped = clampWidth(w);
        if (this.width != clamped) {
            this.width = clamped;
            requestLayout();
            onBoundsChanged();
            if (parent != null) parent.requestLayout();
        }
    }
    public void applyLayoutHeight(int h) {
        int clamped = clampHeight(h);
        if (this.height != clamped) {
            this.height = clamped;
            requestLayout();
            onBoundsChanged();
            if (parent != null) parent.requestLayout();
        }
    }
    public int getBaseWidth() { return baseWidth; }
    public int getBaseHeight() { return baseHeight; }
    public int getMinWidth() { return minWidth; }
    public int getMaxWidth() { return maxWidth; }
    public int getMinHeight() { return minHeight; }
    public int getMaxHeight() { return maxHeight; }
    public float getFlexGrow() { return flexGrow; }
    public float getFlexShrink() { return flexShrink; }
    public void setMinWidth(int minWidth) {
        this.minWidth = Math.max(0, minWidth);
        if (this.maxWidth < this.minWidth) this.maxWidth = this.minWidth;
        setWidth(this.width);
    }
    public void setMaxWidth(int maxWidth) { this.maxWidth = Math.max(this.minWidth, maxWidth); setWidth(this.width); }
    public void setMinHeight(int minHeight) {
        this.minHeight = Math.max(0, minHeight);
        if (this.maxHeight < this.minHeight) this.maxHeight = this.minHeight;
        setHeight(this.height);
    }
    public void setMaxHeight(int maxHeight) { this.maxHeight = Math.max(this.minHeight, maxHeight); setHeight(this.height); }
    public void setFlexGrow(float flexGrow) { this.flexGrow = Math.max(0.0f, flexGrow); if (parent != null) parent.requestLayout(); }
    public void setFlexShrink(float flexShrink) { this.flexShrink = Math.max(0.0f, flexShrink); if (parent != null) parent.requestLayout(); }
    public GuiComponent withMinWidth(int minWidth) { setMinWidth(minWidth); return this; }
    public GuiComponent withMaxWidth(int maxWidth) { setMaxWidth(maxWidth); return this; }
    public GuiComponent withMinHeight(int minHeight) { setMinHeight(minHeight); return this; }
    public GuiComponent withMaxHeight(int maxHeight) { setMaxHeight(maxHeight); return this; }
    public GuiComponent withMinSize(int minW, int minH) { setMinWidth(minW); setMinHeight(minH); return this; }
    public GuiComponent withMaxSize(int maxW, int maxH) { setMaxWidth(maxW); setMaxHeight(maxH); return this; }
    public GuiComponent withFlexGrow(float flexGrow) { setFlexGrow(flexGrow); return this; }
    public GuiComponent withFlexShrink(float flexShrink) { setFlexShrink(flexShrink); return this; }
    public GuiComponent withFlex(float grow, float shrink) { setFlexGrow(grow); setFlexShrink(shrink); return this; }
    public void setX(int x) { if (this.x != x) { this.x = x; invalidateAbs(); onBoundsChanged(); if (parent != null) parent.requestLayout(); } }
    public void setY(int y) { if (this.y != y) { this.y = y; invalidateAbs(); onBoundsChanged(); if (parent != null) parent.requestLayout(); } }
    public void setWidth(int width) {
        int clamped = clampWidth(width);
        this.baseWidth = clamped;
        if (this.width != clamped) {
            this.width = clamped;
            requestLayout();
            onBoundsChanged();
            if (parent != null) parent.requestLayout();
        }
    }
    public void setHeight(int height) {
        int clamped = clampHeight(height);
        this.baseHeight = clamped;
        if (this.height != clamped) {
            this.height = clamped;
            requestLayout();
            onBoundsChanged();
            if (parent != null) parent.requestLayout();
        }
    }
    public void setPadding(com.x4yi.x4ui.client.gui.utils.Insets padding) { this.padding = padding != null ? padding : com.x4yi.x4ui.client.gui.utils.Insets.ZERO; requestLayout(); }
    public void setMargin(com.x4yi.x4ui.client.gui.utils.Insets margin) { this.margin = margin != null ? margin : com.x4yi.x4ui.client.gui.utils.Insets.ZERO; if (parent != null) parent.requestLayout(); }
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