package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.layout.DockSide;
import com.x4yi.x4ui.client.gui.utils.BorderSides;
import com.x4yi.x4ui.client.gui.utils.CornerRadii;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel capable of attaching docked wing panels that visually merge into one
 * continuous background and exterior-only border without internal seam edges.
 */
public class GuiUnionPanel extends GuiPanel {

    public static final class WingEntry {
        public final DockSide side;
        public final GuiPanel wing;
        public final int offset;

        public WingEntry(DockSide side, GuiPanel wing, int offset) {
            this.side = side;
            this.wing = wing;
            this.offset = offset;
        }
    }

    private final List<WingEntry> wings = new ArrayList<>();

    public GuiUnionPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public GuiUnionPanel attachWing(DockSide side, GuiPanel wing) {
        return attachWing(side, wing, 0);
    }

    public GuiUnionPanel attachWing(DockSide side, GuiPanel wing, int offsetAlongEdge) {
        if (side == null || wing == null) return this;
        wing.setSuppressChrome(true);
        wings.add(new WingEntry(side, wing, offsetAlongEdge));
        addChild(wing);
        positionWings();
        return this;
    }

    public void detachWing(GuiPanel wing) {
        if (wing == null) return;
        wings.removeIf(e -> e.wing == wing);
        wing.setSuppressChrome(false);
        removeChild(wing);
    }

    public List<WingEntry> getWings() {
        return wings;
    }

    public void positionWings() {
        for (WingEntry entry : wings) {
            GuiPanel w = entry.wing;
            if (!w.isVisible()) continue;
            switch (entry.side) {
                case LEFT:
                    w.setX(-w.getWidth());
                    w.setY(entry.offset);
                    break;
                case RIGHT:
                    w.setX(this.width);
                    w.setY(entry.offset);
                    break;
                case TOP:
                    w.setX(entry.offset);
                    w.setY(-w.getHeight());
                    break;
                case BOTTOM:
                    w.setX(entry.offset);
                    w.setY(this.height);
                    break;
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        positionWings();
        super.update(deltaTime);
        positionWings();
    }

    public Rectangle getUnionBounds() {
        int minX = getAbsoluteX();
        int minY = getAbsoluteY();
        int maxX = minX + width;
        int maxY = minY + height;
        for (WingEntry entry : wings) {
            if (!entry.wing.isVisible()) continue;
            int wx = entry.wing.getAbsoluteX();
            int wy = entry.wing.getAbsoluteY();
            minX = Math.min(minX, wx);
            minY = Math.min(minY, wy);
            maxX = Math.max(maxX, wx + entry.wing.getWidth());
            maxY = Math.max(maxY, wy + entry.wing.getHeight());
        }
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    protected boolean isMouseOver(int mouseX, int mouseY) {
        if (super.isMouseOver(mouseX, mouseY)) return true;
        for (WingEntry entry : wings) {
            if (entry.wing.isVisible() && entry.wing.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private CornerRadii computeEffectiveMainRadii() {
        CornerRadii r = this.cornerRadii;
        for (WingEntry entry : wings) {
            if (!entry.wing.isVisible()) continue;
            int wx = entry.wing.getX();
            int wy = entry.wing.getY();
            int ww = entry.wing.getWidth();
            int wh = entry.wing.getHeight();
            switch (entry.side) {
                case LEFT:
                    if (wy <= 0 && wy + wh > 0) r = r.withTopLeft(0);
                    if (wy < height && wy + wh >= height) r = r.withBottomLeft(0);
                    break;
                case RIGHT:
                    if (wy <= 0 && wy + wh > 0) r = r.withTopRight(0);
                    if (wy < height && wy + wh >= height) r = r.withBottomRight(0);
                    break;
                case TOP:
                    if (wx <= 0 && wx + ww > 0) r = r.withTopLeft(0);
                    if (wx < width && wx + ww >= width) r = r.withTopRight(0);
                    break;
                case BOTTOM:
                    if (wx <= 0 && wx + ww > 0) r = r.withBottomLeft(0);
                    if (wx < width && wx + ww >= width) r = r.withBottomRight(0);
                    break;
            }
        }
        return r;
    }

    private CornerRadii computeEffectiveWingRadii(WingEntry entry) {
        CornerRadii wr = entry.wing.getCornerRadii();
        if (wr.isZero()) wr = this.cornerRadii;
        switch (entry.side) {
            case LEFT:   return wr.withTopRight(0).withBottomRight(0);
            case RIGHT:  return wr.withTopLeft(0).withBottomLeft(0);
            case TOP:    return wr.withBottomLeft(0).withBottomRight(0);
            case BOTTOM: return wr.withTopLeft(0).withTopRight(0);
            default:     return wr;
        }
    }

    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        CornerRadii effMain = computeEffectiveMainRadii();

        // 1. Disjoint continuous background fill
        if ((backgroundColor & 0xFF000000) != 0) {
            gfx.drawRoundedRect(absX, absY, width, height, effMain, backgroundColor);
        }
        for (WingEntry entry : wings) {
            GuiPanel w = entry.wing;
            if (!w.isVisible()) continue;
            int wBg = (w.getBackgroundColor() & 0xFF000000) != 0 ? w.getBackgroundColor() : this.backgroundColor;
            if ((wBg & 0xFF000000) != 0) {
                gfx.drawRoundedRect(w.getAbsoluteX(), w.getAbsoluteY(), w.getWidth(), w.getHeight(), computeEffectiveWingRadii(entry), wBg);
            }
        }

        // 2. Intelligent exterior border (skipping shared edges)
        if (borderWidth <= 0 || (borderColor & 0xFF000000) == 0) return;

        int mainSides = BorderSides.ALL;
        for (WingEntry entry : wings) {
            if (entry.wing.isVisible()) {
                mainSides = BorderSides.without(mainSides, entry.side.toBorderSide());
            }
        }
        gfx.drawRoundedBorder(absX, absY, width, height, effMain, borderWidth, mainSides, borderColor);

        // Draw uncovered segments on sides that have partial wings
        for (DockSide side : DockSide.values()) {
            if (BorderSides.has(mainSides, side.toBorderSide())) continue;
            drawUncoveredEdgeSegments(gfx, absX, absY, side, borderWidth, borderColor);
        }

        // Draw outer 3 sides of each visible wing
        for (WingEntry entry : wings) {
            GuiPanel w = entry.wing;
            if (!w.isVisible()) continue;
            int wBorderColor = (w.getBorderColor() & 0xFF000000) != 0 ? w.getBorderColor() : this.borderColor;
            int wBorderW = w.getBorderWidth() > 0 ? w.getBorderWidth() : this.borderWidth;
            int wingSides = BorderSides.without(BorderSides.ALL, entry.side.opposite().toBorderSide());
            gfx.drawRoundedBorder(w.getAbsoluteX(), w.getAbsoluteY(), w.getWidth(), w.getHeight(),
                    computeEffectiveWingRadii(entry), wBorderW, wingSides, wBorderColor);
        }
    }

    private void drawUncoveredEdgeSegments(IGraphics gfx, int absX, int absY, DockSide side, int bw, int color) {
        if (side.isHorizontal()) {
            int coveredStart = height;
            int coveredEnd = 0;
            for (WingEntry entry : wings) {
                if (entry.side == side && entry.wing.isVisible()) {
                    coveredStart = Math.min(coveredStart, Math.max(0, entry.wing.getY()));
                    coveredEnd = Math.max(coveredEnd, Math.min(height, entry.wing.getY() + entry.wing.getHeight()));
                }
            }
            int edgeX = (side == DockSide.LEFT) ? absX : (absX + width - bw);
            if (coveredStart > 0) {
                gfx.drawRect(edgeX, absY, bw, coveredStart, color);
            }
            if (coveredEnd < height) {
                gfx.drawRect(edgeX, absY + coveredEnd, bw, height - coveredEnd, color);
            }
        } else {
            int coveredStart = width;
            int coveredEnd = 0;
            for (WingEntry entry : wings) {
                if (entry.side == side && entry.wing.isVisible()) {
                    coveredStart = Math.min(coveredStart, Math.max(0, entry.wing.getX()));
                    coveredEnd = Math.max(coveredEnd, Math.min(width, entry.wing.getX() + entry.wing.getWidth()));
                }
            }
            int edgeY = (side == DockSide.TOP) ? absY : (absY + height - bw);
            if (coveredStart > 0) {
                gfx.drawRect(absX, edgeY, coveredStart, bw, color);
            }
            if (coveredEnd < width) {
                gfx.drawRect(absX + coveredEnd, edgeY, width - coveredEnd, bw, color);
            }
        }
    }
}

