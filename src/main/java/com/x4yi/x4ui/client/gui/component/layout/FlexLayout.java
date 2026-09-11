package com.x4yi.x4ui.client.gui.component.layout;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import java.util.List;
public class FlexLayout implements ILayoutManager {
    public FlexDirection flexDirection = FlexDirection.ABSOLUTE;
    public int gap = 0;
    public boolean flexWrap = false;
    public boolean hugContent = false;
    public FlexLayout() {}
    public FlexLayout(FlexDirection direction, int gap, boolean flexWrap) {
        this(direction, gap, flexWrap, false);
    }
    public FlexLayout(FlexDirection direction, int gap, boolean flexWrap, boolean hugContent) {
        this.flexDirection = direction;
        this.gap = gap;
        this.flexWrap = flexWrap;
        this.hugContent = hugContent;
    }
    public FlexLayout setFlexDirection(FlexDirection direction) {
        this.flexDirection = direction;
        return this;
    }
    public FlexLayout setGap(int gap) {
        this.gap = gap;
        return this;
    }
    public FlexLayout setFlexWrap(boolean flexWrap) {
        this.flexWrap = flexWrap;
        return this;
    }
    public FlexLayout setHugContent(boolean hugContent) {
        this.hugContent = hugContent;
        return this;
    }
    public FlexLayout withHugContent(boolean hugContent) {
        return setHugContent(hugContent);
    }
    public boolean isHugContent() {
        return hugContent;
    }
    @Override
    public void layout(GuiComponent parent, List<GuiComponent> children) {
        if (flexDirection == FlexDirection.ABSOLUTE) return;
        com.x4yi.x4ui.client.gui.utils.Insets padding = parent.getPadding();
        int currentX = padding.left;
        int currentY = padding.top;
        int visibleCount = 0;
        int maxLineSize = 0;
        for (GuiComponent child : children) {
            if (!child.isVisible()) continue;
            visibleCount++;
            com.x4yi.x4ui.client.gui.utils.Insets margin = child.getMargin();
            if (flexDirection == FlexDirection.HORIZONTAL) {
                int childW = child.getWidth() + margin.left + margin.right;
                if (flexWrap && currentX + childW > parent.getWidth() - padding.right && currentX > padding.left) {
                    currentX = padding.left;
                    currentY += maxLineSize + gap;
                    maxLineSize = 0;
                }
                child.setX(currentX + margin.left);
                child.setY(currentY + margin.top);
                currentX += childW + gap;
                maxLineSize = Math.max(maxLineSize, child.getHeight() + margin.top + margin.bottom);
            } else if (flexDirection == FlexDirection.VERTICAL) {
                int childH = child.getHeight() + margin.top + margin.bottom;
                if (flexWrap && currentY + childH > parent.getHeight() - padding.bottom && currentY > padding.top) {
                    currentY = padding.top;
                    currentX += maxLineSize + gap;
                    maxLineSize = 0;
                }
                child.setX(currentX + margin.left);
                child.setY(currentY + margin.top);
                currentY += childH + gap;
                maxLineSize = Math.max(maxLineSize, child.getWidth() + margin.left + margin.right);
            }
        }
        int effectiveContentH = (visibleCount > 0 ? (currentY - gap) : padding.top) + padding.bottom;
        int effectiveContentW = (visibleCount > 0 ? (currentX - gap) : padding.left) + padding.right;
        boolean shouldAdjustParentH = hugContent || (parent.getHeight() == 0 && parent.getPercentHeight() < 0);
        boolean shouldAdjustParentW = hugContent || (parent.getWidth() == 0 && parent.getPercentWidth() < 0);
        if (flexDirection == FlexDirection.VERTICAL) {
            if (!flexWrap && shouldAdjustParentH) parent.setHeight(effectiveContentH);
            else if (flexWrap && shouldAdjustParentW) parent.setWidth(currentX + maxLineSize + padding.right);
        } else if (flexDirection == FlexDirection.HORIZONTAL) {
            if (!flexWrap && shouldAdjustParentW) parent.setWidth(effectiveContentW);
            else if (flexWrap && shouldAdjustParentH) parent.setHeight(currentY + maxLineSize + padding.bottom);
        }
    }
}