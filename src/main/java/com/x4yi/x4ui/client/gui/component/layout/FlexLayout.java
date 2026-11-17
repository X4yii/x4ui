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
            for (GuiComponent child : children) {
                if (child.isVisible()) visible.add(child);
            }
            int n = visible.size();
            int[] resolvedMain = new int[n];
            int[] minMain = new int[n];
            int[] maxMain = new int[n];
            float[] growWeights = new float[n];
            float[] shrinkWeights = new float[n];
            boolean[] frozen = new boolean[n];

            int totalBaseMain = 0;
            int totalMarginsMain = 0;
            int maxCross = 0;
            boolean hasSpacer = false;

            for (int i = 0; i < n; i++) {
                GuiComponent child = visible.get(i);
                Insets margin = child.getMargin();
                if (isSpacer(child)) {
                    hasSpacer = true;
                    resolvedMain[i] = 0;
                    minMain[i] = 0;
                    maxMain[i] = Integer.MAX_VALUE;
                    growWeights[i] = child.getFlexGrow() > 0.0f ? child.getFlexGrow() : 1.0f;
                    shrinkWeights[i] = 0.0f;
                    continue;
                }
                if (flexDirection == FlexDirection.HORIZONTAL) {
                    if (alignItems == FlexAlign.STRETCH && !shouldAdjustParentH && child.getPercentHeight() < 0) {
                        child.applyLayoutHeight(Math.max(0, innerH - margin.top - margin.bottom));
                    }
                    int rawBase = (child.getFlexGrow() > 0.0f || child.getFlexShrink() > 0.0f) ? child.getBaseWidth() : child.getWidth();
                    int base = child.clampWidth(rawBase);
                    resolvedMain[i] = base;
                    minMain[i] = child.getMinWidth();
                    maxMain[i] = child.getMaxWidth();
                    growWeights[i] = child.getFlexGrow();
                    shrinkWeights[i] = child.getFlexShrink() * Math.max(1, base);
                    totalBaseMain += base;
                    totalMarginsMain += margin.left + margin.right;
                    maxCross = Math.max(maxCross, child.getHeight() + margin.top + margin.bottom);
                } else {
                    if (alignItems == FlexAlign.STRETCH && !shouldAdjustParentW && child.getPercentWidth() < 0) {
                        child.applyLayoutWidth(Math.max(0, innerW - margin.left - margin.right));
                    }
                    int rawBase = (child.getFlexGrow() > 0.0f || child.getFlexShrink() > 0.0f) ? child.getBaseHeight() : child.getHeight();
                    int base = child.clampHeight(rawBase);
                    resolvedMain[i] = base;
                    minMain[i] = child.getMinHeight();
                    maxMain[i] = child.getMaxHeight();
                    growWeights[i] = child.getFlexGrow();
                    shrinkWeights[i] = child.getFlexShrink() * Math.max(1, base);
                    totalBaseMain += base;
                    totalMarginsMain += margin.top + margin.bottom;
                    maxCross = Math.max(maxCross, child.getWidth() + margin.left + margin.right);
                }
            }

            int totalGaps = n > 1 ? gap * (n - 1) : 0;
            int usedMain = totalBaseMain + totalMarginsMain + totalGaps;
            int availableMain = (flexDirection == FlexDirection.HORIZONTAL) ? innerW : innerH;
            int deltaMain = availableMain - usedMain;
            boolean adjustMain = (flexDirection == FlexDirection.HORIZONTAL) ? shouldAdjustParentW : shouldAdjustParentH;
            boolean canGrow = !adjustMain || (hasSpacer && availableMain > 0);
            boolean canShrink = !adjustMain;

            if (deltaMain > 0 && canGrow) {
                int remaining = deltaMain;
                while (remaining > 0) {
                    double totalGrow = 0.0;
                    for (int i = 0; i < n; i++) {
                        if (!frozen[i] && growWeights[i] > 0.0f && resolvedMain[i] < maxMain[i]) {
                            totalGrow += growWeights[i];
                        } else if (!frozen[i] && (growWeights[i] <= 0.0f || resolvedMain[i] >= maxMain[i])) {
                            frozen[i] = true;
                        }
                    }
                    if (totalGrow <= 0.0) break;

                    int distributedThisPass = 0;
                    boolean hitBound = false;
                    int activeRemaining = remaining;
                    for (int i = 0; i < n; i++) {
                        if (frozen[i]) continue;
                        int share = (int) Math.floor(activeRemaining * (growWeights[i] / totalGrow));
                        if (share == 0 && remaining - distributedThisPass > 0) {
                            share = 1;
                        }
                        int room = maxMain[i] - resolvedMain[i];
                        int add = Math.min(share, Math.min(room, remaining - distributedThisPass));
                        if (add > 0) {
                            resolvedMain[i] += add;
                            distributedThisPass += add;
                        }
                        if (resolvedMain[i] >= maxMain[i]) {
                            frozen[i] = true;
                            hitBound = true;
                        }
                    }
                    remaining -= distributedThisPass;
                    if (distributedThisPass == 0 && !hitBound) break;
                }
            } else if (deltaMain < 0 && canShrink) {
                int remainingOverflow = -deltaMain;
                while (remainingOverflow > 0) {
                    double totalShrink = 0.0;
                    for (int i = 0; i < n; i++) {
                        if (!frozen[i] && shrinkWeights[i] > 0.0f && resolvedMain[i] > minMain[i]) {
                            totalShrink += shrinkWeights[i];
                        } else if (!frozen[i] && (shrinkWeights[i] <= 0.0f || resolvedMain[i] <= minMain[i])) {
                            frozen[i] = true;
                        }
                    }
                    if (totalShrink <= 0.0) break;

                    int reducedThisPass = 0;
                    boolean hitBound = false;
                    int activeOverflow = remainingOverflow;
                    for (int i = 0; i < n; i++) {
                        if (frozen[i]) continue;
                        int share = (int) Math.floor(activeOverflow * (shrinkWeights[i] / totalShrink));
                        if (share == 0 && remainingOverflow - reducedThisPass > 0) {
                            share = 1;
                        }
                        int room = resolvedMain[i] - minMain[i];
                        int sub = Math.min(share, Math.min(room, remainingOverflow - reducedThisPass));
                        if (sub > 0) {
                            resolvedMain[i] -= sub;
                            reducedThisPass += sub;
                        }
                        if (resolvedMain[i] <= minMain[i]) {
                            frozen[i] = true;
                            hitBound = true;
                        }
                    }
                    remainingOverflow -= reducedThisPass;
                    if (reducedThisPass == 0 && !hitBound) break;
                }
            }

            int finalUsedMain = totalMarginsMain + totalGaps;
            for (int i = 0; i < n; i++) {
                finalUsedMain += resolvedMain[i];
            }
            int freeMain = adjustMain ? 0 : Math.max(0, availableMain - finalUsedMain);
            int mainOffset = 0;
            if (freeMain > 0) {
                if (justifyContent == FlexAlign.CENTER) {
                    mainOffset = freeMain / 2;
                } else if (justifyContent == FlexAlign.END) {
                    mainOffset = freeMain;
                }
            }

            int currentMain = (flexDirection == FlexDirection.HORIZONTAL ? padding.left : padding.top) + mainOffset;
            for (int i = 0; i < n; i++) {
                GuiComponent child = visible.get(i);
                Insets margin = child.getMargin();
                if (isSpacer(child)) {
                    if (flexDirection == FlexDirection.HORIZONTAL) {
                        child.setX(currentMain);
                        child.setY(padding.top);
                        child.applyLayoutWidth(resolvedMain[i]);
                        child.applyLayoutHeight(0);
                    } else {
                        child.setX(padding.left);
                        child.setY(currentMain);
                        child.applyLayoutWidth(0);
                        child.applyLayoutHeight(resolvedMain[i]);
                    }
                    currentMain += resolvedMain[i] + gap;
                    continue;
                }

                if (flexDirection == FlexDirection.HORIZONTAL) {
                    child.applyLayoutWidth(resolvedMain[i]);
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
                    currentMain += resolvedMain[i] + margin.left + margin.right + gap;
                } else {
                    child.applyLayoutHeight(resolvedMain[i]);
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
                    currentMain += resolvedMain[i] + margin.top + margin.bottom + gap;
                }
            }

            if (flexDirection == FlexDirection.VERTICAL) {
                int contentH = padding.top + (visible.isEmpty() ? 0 : finalUsedMain) + padding.bottom;
                if (shouldAdjustParentH) parent.applyLayoutHeight(contentH);
                if (shouldAdjustParentW || (parent.getWidth() == 0 && parent.getPercentWidth() < 0)) {
                    parent.applyLayoutWidth(padding.left + maxCross + padding.right);
                }
            } else {
                int contentW = padding.left + (visible.isEmpty() ? 0 : finalUsedMain) + padding.right;
                if (shouldAdjustParentW) parent.applyLayoutWidth(contentW);
                if (shouldAdjustParentH) parent.applyLayoutHeight(padding.top + maxCross + padding.bottom);
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
            parent.applyLayoutWidth(currentX + maxLineSize + padding.right);
        } else if (flexDirection == FlexDirection.HORIZONTAL && shouldAdjustParentH) {
            parent.applyLayoutHeight(currentY + maxLineSize + padding.bottom);
        }
    }
}
