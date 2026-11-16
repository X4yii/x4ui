package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.utils.Insets;
import net.minecraft.inventory.Slot;
import java.util.ArrayList;
import java.util.List;

public class GuiGridPanel extends GuiScrollPanel {
    private int cellWidth;
    private int cellHeight;
    private int gapX;
    private int gapY;
    private int fixedColumns = 0;
    private int computedColumns = 1;
    private boolean gridDirty = true;
    private int lastGridWidth = -1;
    private int lastGridHeight = -1;
    private int lastChildCount = -1;

    public GuiGridPanel(int x, int y, int width, int height, int cellWidth, int cellHeight) {
        this(x, y, width, height, cellWidth, cellHeight, 2, 2);
    }

    public GuiGridPanel(int x, int y, int width, int height, int cellWidth, int cellHeight, int gapX, int gapY) {
        super(x, y, width, height);
        this.cellWidth = Math.max(1, cellWidth);
        this.cellHeight = Math.max(1, cellHeight);
        this.gapX = Math.max(0, gapX);
        this.gapY = Math.max(0, gapY);
        this.contentPanel.setFlexDirection(FlexDirection.ABSOLUTE);
        this.contentPanel.setLayoutManager(null);
    }

    public static GuiGridPanel slotGrid(int height, int gap) {
        GuiGridPanel grid = new GuiGridPanel(0, 0, 0, height, 18, 18, gap, gap);
        grid.withPercentWidth(1.0f);
        return grid;
    }

    public GuiGridPanel withColumns(int fixedColumns) {
        this.fixedColumns = Math.max(0, fixedColumns);
        requestLayout();
        return this;
    }

    public GuiGridPanel withAutoColumns() {
        this.fixedColumns = 0;
        requestLayout();
        return this;
    }

    public GuiGridPanel withCellSize(int cellWidth, int cellHeight) {
        this.cellWidth = Math.max(1, cellWidth);
        this.cellHeight = Math.max(1, cellHeight);
        requestLayout();
        return this;
    }

    public GuiGridPanel withCellGap(int gapX, int gapY) {
        this.gapX = Math.max(0, gapX);
        this.gapY = Math.max(0, gapY);
        requestLayout();
        return this;
    }

    public int getComputedColumns() {
        return computedColumns;
    }

    public GuiGridPanel addSlots(Iterable<Slot> slots, GuiBaseContainer screen) {
        if (slots == null || screen == null) return this;
        for (Slot slot : slots) {
            addChild(new GuiSlot(slot, screen));
        }
        return this;
    }

    public int computeColumnsForWidth(int availableWidth) {
        if (fixedColumns > 0) return fixedColumns;
        int usable = Math.max(0, availableWidth);
        return Math.max(1, (usable + gapX) / (cellWidth + gapX));
    }

    public void layoutGrid() {
        List<GuiComponent> visible = new ArrayList<>();
        for (GuiComponent child : contentPanel.getChildren()) {
            if (child.isVisible()) visible.add(child);
        }

        Insets pad = contentPanel.getPadding();
        int baseInnerW = Math.max(0, this.width - pad.left - pad.right);
        int cols = computeColumnsForWidth(baseInnerW);
        int rows = visible.isEmpty() ? 0 : (visible.size() + cols - 1) / cols;
        int totalH = pad.top + pad.bottom + (rows > 0 ? (rows * cellHeight + (rows - 1) * gapY) : 0);

        boolean needsScroll = this.height > 0 && totalH > this.height;
        int reservedRight = needsScroll ? (this.scrollbarWidth + 6) : 0;

        if (needsScroll) {
            int innerWithScroll = Math.max(0, baseInnerW - reservedRight);
            cols = computeColumnsForWidth(innerWithScroll);
            rows = visible.isEmpty() ? 0 : (visible.size() + cols - 1) / cols;
            totalH = pad.top + pad.bottom + (rows > 0 ? (rows * cellHeight + (rows - 1) * gapY) : 0);
        }

        this.computedColumns = cols;
        if (contentPanel.getMargin().right != reservedRight) {
            Insets m = contentPanel.getMargin();
            contentPanel.setMargin(new Insets(m.top, reservedRight, m.bottom, m.left));
        }
        contentPanel.applyLayoutWidth(Math.max(0, this.width - reservedRight));
        contentPanel.applyLayoutHeight(totalH);

        for (int i = 0; i < visible.size(); i++) {
            GuiComponent child = visible.get(i);
            int col = i % cols;
            int row = i / cols;
            child.setX(pad.left + col * (cellWidth + gapX));
            child.setY(pad.top + row * (cellHeight + gapY));
            child.applyLayoutWidth(cellWidth);
            child.applyLayoutHeight(cellHeight);
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        this.gridDirty = true;
    }

    @Override
    protected void onBoundsChanged() {
        super.onBoundsChanged();
        if (this.width != lastGridWidth || this.height != lastGridHeight) {
            this.gridDirty = true;
        }
    }

    private int lastVisibleMask = -1;

    private int computeVisibleSignature() {
        int sig = 1;
        for (GuiComponent c : contentPanel.getChildren()) {
            sig = 31 * sig + (c.isVisible() ? 1 : 0);
        }
        return sig;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        int currentCount = contentPanel.getChildren().size();
        int visSig = computeVisibleSignature();
        if (gridDirty || this.width != lastGridWidth || this.height != lastGridHeight
                || currentCount != lastChildCount || visSig != lastVisibleMask) {
            layoutGrid();
            this.gridDirty = false;
            this.lastGridWidth = this.width;
            this.lastGridHeight = this.height;
            this.lastChildCount = currentCount;
            this.lastVisibleMask = visSig;
        }
    }
}

