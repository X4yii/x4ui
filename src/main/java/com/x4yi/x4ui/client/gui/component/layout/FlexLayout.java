package com.x4yi.x4ui.client.gui.component.layout;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import java.util.List;
public class FlexLayout implements ILayoutManager {
    public FlexDirection flexDirection = FlexDirection.ABSOLUTE;
    public int gap = 0;
    public boolean flexWrap = false;
    public FlexLayout() {}
    public FlexLayout(FlexDirection direction, int gap, boolean flexWrap) {
        this.flexDirection = direction;
        this.gap = gap;
        this.flexWrap = flexWrap;
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
    @Override
    public void layout(GuiComponent parent, List<GuiComponent> children) {
        if (flexDirection == FlexDirection.ABSOLUTE) return;
        com.x4yi.x4ui.client.gui.utils.Insets padding = parent.getPadding();
        int currentX = padding.left;
        int currentY = padding.top;
        int maxLineSize = 0;
        for (GuiComponent child : children) {
            if (!child.isVisible()) continue; // Draggable was removed
            com.x4yi.x4ui.client.gui.utils.Insets margin = child.getMargin();
            if (flexDirection == FlexDirection.HORIZONTAL) {
                int childW = child.getWidth() + margin.left + margin.right;
                if (flexWrap && currentX + childW > parent.getWidth() - padding.right) {
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
                if (flexWrap && currentY + childH > parent.getHeight() - padding.bottom) {
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
        if (flexDirection == FlexDirection.VERTICAL) {
            if (!flexWrap) parent.setHeight(currentY + padding.bottom);
            else parent.setWidth(currentX + maxLineSize + padding.right);
        } else if (flexDirection == FlexDirection.HORIZONTAL) {
            if (!flexWrap) parent.setWidth(currentX + padding.right);
            else parent.setHeight(currentY + maxLineSize + padding.bottom);
        }
    }
}