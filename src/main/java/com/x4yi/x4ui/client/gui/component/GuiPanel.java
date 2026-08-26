package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
public class GuiPanel extends GuiComponent {
    protected FlexDirection flexDirection = FlexDirection.ABSOLUTE;
    protected int gap = 0;
    protected boolean flexWrap = false;
    public GuiPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public void setFlexDirection(FlexDirection flexDirection) {
        this.flexDirection = flexDirection;
    }
    public FlexDirection getFlexDirection() {
        return this.flexDirection;
    }
    public void setGap(int gap) {
        this.gap = gap;
    }
    public void setFlexWrap(boolean flexWrap) {
        this.flexWrap = flexWrap;
    }
    @Override
    public void update() {
        super.update();
        if (needsLayout) {
            needsLayout = false;
            layoutChildren();
        }
    }
    protected void layoutChildren() {
        if (flexDirection == FlexDirection.ABSOLUTE) return;
        int oldWidth = this.width;
        int oldHeight = this.height;
        int currentX = padding;
        int currentY = padding;
        int maxLineSize = 0;
        for (GuiComponent child : children) {
            if (!child.isVisible() || child.isDraggable()) continue;
            if (flexDirection == FlexDirection.HORIZONTAL) {
                int childW = child.getWidth() + child.getMargin() * 2;
                if (flexWrap && currentX + childW > this.width - padding) {
                    currentX = padding;
                    currentY += maxLineSize + gap;
                    maxLineSize = 0;
                }
                child.setX(currentX + child.getMargin());
                child.setY(currentY + child.getMargin());
                currentX += childW + gap;
                maxLineSize = Math.max(maxLineSize, child.getHeight() + child.getMargin() * 2);
            } else if (flexDirection == FlexDirection.VERTICAL) {
                int childH = child.getHeight() + child.getMargin() * 2;
                if (flexWrap && currentY + childH > this.height - padding) {
                    currentY = padding;
                    currentX += maxLineSize + gap;
                    maxLineSize = 0;
                }
                child.setX(currentX + child.getMargin());
                child.setY(currentY + child.getMargin());
                currentY += childH + gap;
                maxLineSize = Math.max(maxLineSize, child.getWidth() + child.getMargin() * 2);
            }
        }
        if (flexDirection == FlexDirection.VERTICAL) {
            if (!flexWrap) this.height = currentY + padding;
            else this.width = currentX + maxLineSize + padding;
        } else if (flexDirection == FlexDirection.HORIZONTAL) {
            if (!flexWrap) this.width = currentX + padding;
            else this.height = currentY + maxLineSize + padding;
        }
        if ((oldWidth != this.width || oldHeight != this.height) && parent != null) {
            parent.markDirty();
        }
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
    }
}