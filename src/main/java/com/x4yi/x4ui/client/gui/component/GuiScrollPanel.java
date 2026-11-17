package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.utils.ITheme;
public class GuiScrollPanel extends GuiPanel {
    protected float scrollY = 0;
    protected float targetScrollY = 0;
    protected int maxScrollY = 0;
    protected float scrollSpeed = 0.2f;
    protected int scrollStep = 22;
    protected int scrollbarWidth = 4;
    protected boolean isDraggingThumb = false;
    protected int dragStartY = 0;
    protected float dragStartScrollY = 0;
    protected GuiPanel contentPanel;
    public GuiScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setFlexDirection(FlexDirection.ABSOLUTE);
        contentPanel = new GuiPanel(0, 0, width, height) {
            @Override
            protected void renderChildren(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
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
                        child.render(gfx, mouseX, mouseY, partialTicks);
                    }
                }
            }
            @Override
            public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
                if (!visible || !enabled || !GuiScrollPanel.this.isMouseOver(mouseX, mouseY)) return false;
                ensureRenderChildren();
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
            @Override
            public GuiComponent getHoveredComponent(int mouseX, int mouseY) {
                if (!visible || !GuiScrollPanel.this.isMouseOver(mouseX, mouseY)) return null;
                return super.getHoveredComponent(mouseX, mouseY);
            }
        };
        contentPanel.setFlexDirection(FlexDirection.VERTICAL);
        contentPanel.setHugContent(true);
        contentPanel.withPercentWidth(1.0f);
        super.addChild(contentPanel);
    }
    public GuiPanel getContentPanel() {
        return contentPanel;
    }

    public static GuiScrollPanel vertical(int height, int gap) {
        GuiScrollPanel sp = new GuiScrollPanel(0, 0, 0, height);
        sp.withPercentWidth(1.0f);
        sp.setContentGap(gap);
        return sp;
    }

    public GuiScrollPanel withContentGap(int gap) {
        setContentGap(gap);
        return this;
    }

    public GuiScrollPanel withContentPadding(int padding) {
        setContentPadding(new com.x4yi.x4ui.client.gui.utils.Insets(padding));
        return this;
    }
    public float getScrollY() { return scrollY; }
    public void setScrollY(float y) { this.scrollY = y; }
    public float getTargetScrollY() { return targetScrollY; }
    public void setTargetScrollY(float y) { this.targetScrollY = y; }
    public int getMaxScrollY() { return maxScrollY; }
    public void scrollToBottom() { this.targetScrollY = this.maxScrollY; }
    public int getScrollbarWidth() { return scrollbarWidth; }
    public void setScrollbarWidth(int scrollbarWidth) { this.scrollbarWidth = Math.max(1, scrollbarWidth); }
    public GuiScrollPanel withScrollbarWidth(int width) { setScrollbarWidth(width); return this; }
    public int getScrollStep() { return scrollStep; }
    public void setScrollStep(int step) { this.scrollStep = step; }
    public GuiScrollPanel withScrollStep(int step) { setScrollStep(step); return this; }
    public float getScrollSpeed() { return scrollSpeed; }
    public void setScrollSpeed(float speed) { this.scrollSpeed = speed; }
    public GuiScrollPanel withScrollSpeed(float speed) { setScrollSpeed(speed); return this; }
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
    public java.util.List<GuiComponent> getChildren() {
        return contentPanel.getChildren();
    }
    public void setContentGap(int gap) {
        contentPanel.setGap(gap);
    }
    public void setContentPadding(com.x4yi.x4ui.client.gui.utils.Insets padding) {
        contentPanel.setPadding(padding);
    }
    public void setContentFlexDirection(FlexDirection direction) {
        contentPanel.setFlexDirection(direction);
    }
    public void setContentFlexWrap(boolean flexWrap) {
        contentPanel.setFlexWrap(flexWrap);
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        maxScrollY = Math.max(0, contentPanel.getHeight() - this.height);
        int desiredRightMargin = maxScrollY > 0 ? (scrollbarWidth + 6) : 0;
        if (contentPanel.getMargin().right != desiredRightMargin) {
            com.x4yi.x4ui.client.gui.utils.Insets m = contentPanel.getMargin();
            contentPanel.setMargin(new com.x4yi.x4ui.client.gui.utils.Insets(m.top, desiredRightMargin, m.bottom, m.left));
        }
        if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
        if (targetScrollY < 0) targetScrollY = 0;
        float dt = (deltaTime > 0.0f) ? deltaTime : (com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime > 0.0f ? com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime : 0.016f);
        float factor = Math.min(1.0f, Math.max(0.0f, scrollSpeed * 60f * dt));
        scrollY += (targetScrollY - scrollY) * factor;
        if (Math.abs(targetScrollY - scrollY) < 0.5f) {
            scrollY = targetScrollY;
        }
        scrollY = Math.max(0.0f, Math.min((float) maxScrollY, scrollY));
        contentPanel.setY(-(int) scrollY);
    }
    @Override
    protected void renderChildren(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        gfx.pushClip(absX, absY, width, height);
        try {
            super.renderChildren(gfx, mouseX, mouseY, partialTicks);
        } finally {
            gfx.popClip();
        }
        if (maxScrollY > 0) {
            ITheme theme = getTheme();
            int trackX = absX + width - scrollbarWidth - 2;
            int trackY = absY;
            int trackHeight = height;
            gfx.drawRect(trackX, trackY, scrollbarWidth, trackHeight, theme.getScrollbarTrackColor());
            float scrollRatio = scrollY / maxScrollY;
            int thumbHeight = (contentPanel.getHeight() > 0) ? Math.max(20, (int) ((float) height / contentPanel.getHeight() * trackHeight)) : 20;
            int thumbY = trackY + (int) (scrollRatio * (trackHeight - thumbHeight));
            int thumbColor = isDraggingThumb ? 0xFF64B5F6 : theme.getScrollbarThumbColor();
            gfx.drawRect(trackX, thumbY, scrollbarWidth, thumbHeight, thumbColor);
        }
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled || !isMouseOver(mouseX, mouseY)) return false;
        if (mouseButton == 0 && maxScrollY > 0) {
            int absX = getAbsoluteX();
            int absY = getAbsoluteY();
            int trackHitbox = Math.max(8, scrollbarWidth + 4);
            int trackAreaX = absX + width - trackHitbox;
            if (mouseX >= trackAreaX && mouseX <= absX + width && mouseY >= absY && mouseY <= absY + height) {
                int trackY = absY;
                int trackHeight = height;
                int thumbHeight = (contentPanel.getHeight() > 0) ? Math.max(20, (int) ((float) height / contentPanel.getHeight() * trackHeight)) : 20;
                float scrollRatio = (maxScrollY > 0) ? (scrollY / maxScrollY) : 0;
                int thumbY = trackY + (int) (scrollRatio * (trackHeight - thumbHeight));
                if (mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                    isDraggingThumb = true;
                    dragStartY = mouseY;
                    dragStartScrollY = scrollY;
                    requestScrollFocus(this);
                    return true;
                } else {
                    int availableTrack = trackHeight - thumbHeight;
                    if (availableTrack > 0) {
                        float clickRatio = (float) (mouseY - trackY - thumbHeight / 2) / availableTrack;
                        targetScrollY = Math.max(0, Math.min(maxScrollY, clickRatio * maxScrollY));
                        scrollY = targetScrollY;
                        isDraggingThumb = true;
                        dragStartY = mouseY;
                        dragStartScrollY = scrollY;
                        requestScrollFocus(this);
                        return true;
                    }
                }
            }
        }
        return super.onMouseClick(mouseX, mouseY, mouseButton);
    }
    @Override
    public boolean onMouseDrag(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!visible || !enabled) return false;
        if (isDraggingThumb && clickedMouseButton == 0 && maxScrollY > 0) {
            int trackHeight = height;
            int thumbHeight = (contentPanel.getHeight() > 0) ? Math.max(20, (int) ((float) height / contentPanel.getHeight() * trackHeight)) : 20;
            int availableTrack = trackHeight - thumbHeight;
            if (availableTrack > 0) {
                int deltaY = mouseY - dragStartY;
                float deltaScroll = (float) deltaY / availableTrack * maxScrollY;
                targetScrollY = Math.max(0, Math.min(maxScrollY, dragStartScrollY + deltaScroll));
                scrollY = targetScrollY;
                requestScrollFocus(this);
            }
            return true;
        }
        return super.onMouseDrag(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int state) {
        if (isDraggingThumb) {
            isDraggingThumb = false;
            clearScrollFocus();
            return true;
        }
        return super.onMouseRelease(mouseX, mouseY, state);
    }
    @Override
    public boolean onMouseScroll(int mouseX, int mouseY, int wheel) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        if (getRoot().getScrollFocusedComponent() == this) {
            if (wheel != 0 && maxScrollY > 0) {
                int direction = wheel > 0 ? -1 : 1;
                targetScrollY += direction * scrollStep;
                if (targetScrollY < 0) targetScrollY = 0;
                if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
                requestScrollFocus(this);
                return true;
            }
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
            requestScrollFocus(this);
            return true;
        }
        return false;
    }
}