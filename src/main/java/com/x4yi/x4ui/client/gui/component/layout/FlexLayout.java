package com.x4yi.x4ui.client.gui.component.layout;

import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.client.gui.utils.Insets;
import java.util.ArrayList;
import java.util.List;

public class FlexLayout implements ILayoutManager {
    public FlexDirection flexDirection = FlexDirection.ABSOLUTE;
    public int gap = 0;
    public boolean flexWrap = false;
    public boolean hugContent = false;
    public FlexAlign alignItems = FlexAlign.START;
    public FlexAlign justifyContent = FlexAlign.START;

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

    public FlexLayout setAlignItems(FlexAlign alignItems) {
        this.alignItems = alignItems != null ? alignItems : FlexAlign.START;
        return this;
    }

    public FlexLayout withAlignItems(FlexAlign alignItems) {
        return setAlignItems(alignItems);
    }

    public FlexLayout setJustifyContent(FlexAlign justifyContent) {
        this.justifyContent = justifyContent != null ? justifyContent : FlexAlign.START;
        return this;
    }

    public FlexLayout withJustifyContent(FlexAlign justifyContent) {
        return setJustifyContent(justifyContent);
    }

    private static boolean isSpacer(GuiComponent child) {
        return child instanceof GuiPanel && ((GuiPanel) child).isFlexSpacer();
    }

    @Override
    public void layout(GuiComponent parent, List<GuiComponent> children) {
        if (flexDirection == FlexDirection.ABSOLUTE) return;

        Insets padding = parent.getPadding();
        int innerW = Math.max(0, parent.getWidth() - padding.left - padding.right);
        int innerH = Math.max(0, parent.getHeight() - padding.top - padding.bottom);

        boolean shouldAdjustParentH = parent.getPercentHeight() < 0 && (hugContent || parent.getHeight() == 0);
        boolean shouldAdjustParentW = parent.getPercentWidth() < 0 && (hugContent || parent.getWidth() == 0);

        if (!flexWrap) {
            List<GuiComponent> visible = new ArrayList<>();
            int totalMainFixed = 0;
            int spacerCount = 0;
            int maxCross = 0;

            for (GuiComponent child : children) {
                if (!child.isVisible()) continue;
                visible.add(child);
                Insets margin = child.getMargin();
                if (isSpacer(child)) {
                    spacerCount++;
                } else if (flexDirection == FlexDirection.HORIZONTAL) {
                    if (alignItems == FlexAlign.STRETCH && !shouldAdjustParentH && child.getPercentHeight() < 0) {
                        child.setHeight(Math.max(0, innerH - margin.top - margin.bottom));
                    }
                    totalMainFixed += child.getWidth() + margin.left + margin.right;
                    maxCross = Math.max(maxCross, child.getHeight() + margin.top + margin.bottom);
                } else {
                    if (alignItems == FlexAlign.STRETCH && !shouldAdjustParentW && child.getPercentWidth() < 0) {
                        child.setWidth(Math.max(0, innerW - margin.left - margin.right));
                    }
                    totalMainFixed += child.getHeight() + margin.top + margin.bottom;
                    maxCross = Math.max(maxCross, child.getWidth() + margin.left + margin.right);
                }
            }

            int totalGaps = visible.size() > 1 ? gap * (visible.size() - 1) : 0;
            int usedMain = totalMainFixed + totalGaps;
            int availableMain = (flexDirection == FlexDirection.HORIZONTAL) ? innerW : innerH;
            int freeMain = Math.max(0, availableMain - usedMain);

            int spacerEach = (spacerCount > 0) ? (freeMain / spacerCount) : 0;
            int spacerRem = (spacerCount > 0) ? (freeMain % spacerCount) : 0;

            int mainOffset = 0;
            if (spacerCount == 0 && freeMain > 0) {
                if (justifyContent == FlexAlign.CENTER) {
                    mainOffset = freeMain / 2;
                } else if (justifyContent == FlexAlign.END) {
                    mainOffset = freeMain;
                }
            }

            int currentMain = (flexDirection == FlexDirection.HORIZONTAL ? padding.left : padding.top) + mainOffset;
            int spacerSeen = 0;

            for (GuiComponent child : visible) {
                Insets margin = child.getMargin();
                if (isSpacer(child)) {
                    int extra = spacerEach + (spacerSeen < spacerRem ? 1 : 0);
                    spacerSeen++;
                    if (flexDirection == FlexDirection.HORIZONTAL) {
                        child.setX(currentMain);
                        child.setY(padding.top);
                        child.setWidth(extra);
                        child.setHeight(0);
                    } else {
                        child.setX(padding.left);
                        child.setY(currentMain);
                        child.setWidth(0);
                        child.setHeight(extra);
                    }
                    currentMain += extra + gap;
                    continue;
                }

                if (flexDirection == FlexDirection.HORIZONTAL) {
                    int childCross = child.getHeight() + margin.top + margin.bottom;
                    int crossBase = shouldAdjustParentH ? maxCross : innerH;
                    int crossOffset = 0;
                    if (alignItems == FlexAlign.CENTER) {
                        crossOffset = Math.max(0, (crossBase - childCross) / 2);
                    } else if (alignItems == FlexAlign.END) {
                        crossOffset = Math.max(0, crossBase - childCross);
                    }
                    child.setX(currentMain + margin.left);
                    child.setY(padding.top + crossOffset + margin.top);
                    currentMain += child.getWidth() + margin.left + margin.right + gap;
                } else {
                    int childCross = child.getWidth() + margin.left + margin.right;
                    int crossBase = shouldAdjustParentW ? maxCross : innerW;
                    int crossOffset = 0;
                    if (alignItems == FlexAlign.CENTER) {
                        crossOffset = Math.max(0, (crossBase - childCross) / 2);
                    } else if (alignItems == FlexAlign.END) {
                        crossOffset = Math.max(0, crossBase - childCross);
                    }
                    child.setX(padding.left + crossOffset + margin.left);
                    child.setY(currentMain + margin.top);
                    currentMain += child.getHeight() + margin.top + margin.bottom + gap;
                }
            }

            if (flexDirection == FlexDirection.VERTICAL) {
                int contentH = (visible.isEmpty() ? padding.top : (currentMain - gap)) + padding.bottom;
                if (shouldAdjustParentH) parent.setHeight(contentH);
                if (parent.getWidth() == 0 && parent.getPercentWidth() < 0) {
                    parent.setWidth(padding.left + maxCross + padding.right);
                }
            } else {
                int contentW = (visible.isEmpty() ? padding.left : (currentMain - gap)) + padding.right;
                if (shouldAdjustParentW) parent.setWidth(contentW);
                if (shouldAdjustParentH) parent.setHeight(padding.top + maxCross + padding.bottom);
            }
            return;
        }

        int currentX = padding.left;
        int currentY = padding.top;
        int maxLineSize = 0;
        for (GuiComponent child : children) {
            if (!child.isVisible()) continue;
            Insets margin = child.getMargin();
            if (flexDirection == FlexDirection.HORIZONTAL) {
                int childW = child.getWidth() + margin.left + margin.right;
                if (currentX + childW > parent.getWidth() - padding.right && currentX > padding.left) {
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
                if (currentY + childH > parent.getHeight() - padding.bottom && currentY > padding.top) {
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
        if (flexDirection == FlexDirection.VERTICAL && shouldAdjustParentW) {
            parent.setWidth(currentX + maxLineSize + padding.right);
        } else if (flexDirection == FlexDirection.HORIZONTAL && shouldAdjustParentH) {
            parent.setHeight(currentY + maxLineSize + padding.bottom);
        }
    }
}
