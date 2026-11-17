package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.layout.DockSide;
import com.x4yi.x4ui.client.gui.utils.BorderSides;
import com.x4yi.x4ui.client.gui.utils.CornerRadii;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GuiUnionPanel extends GuiPanel {

    private static final DockSide[] ALL_SIDES = DockSide.values();

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
    private final List<int[]> scratchIntervals = new ArrayList<>(4);

    public GuiUnionPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public static GuiUnionPanel of(int width, int height) {
        return new GuiUnionPanel(0, 0, width, height);
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

    private static boolean isActiveWing(GuiPanel w) {
        return w.isVisible() && w.getWidth() > 0 && w.getHeight() > 0;
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

    private void accumulateUnionBounds(int[] bounds) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (absX < bounds[0]) bounds[0] = absX;
        if (absY < bounds[1]) bounds[1] = absY;
        if (absX + width > bounds[2]) bounds[2] = absX + width;
        if (absY + height > bounds[3]) bounds[3] = absY + height;
        for (WingEntry entry : wings) {
            if (!isActiveWing(entry.wing)) continue;
            if (entry.wing instanceof GuiUnionPanel) {
                ((GuiUnionPanel) entry.wing).accumulateUnionBounds(bounds);
            } else {
                int wx = entry.wing.getAbsoluteX();
                int wy = entry.wing.getAbsoluteY();
                if (wx < bounds[0]) bounds[0] = wx;
                if (wy < bounds[1]) bounds[1] = wy;
                if (wx + entry.wing.getWidth() > bounds[2]) bounds[2] = wx + entry.wing.getWidth();
                if (wy + entry.wing.getHeight() > bounds[3]) bounds[3] = wy + entry.wing.getHeight();
            }
        }
    }

    public Rectangle getUnionBounds() {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        int[] b = new int[] { absX, absY, absX + width, absY + height };
        accumulateUnionBounds(b);
        return new Rectangle(b[0], b[1], b[2] - b[0], b[3] - b[1]);
    }

    @Override
    protected boolean isClippedBy(IGraphics gfx) {
        int minX = getAbsoluteX();
        int minY = getAbsoluteY();
        int maxX = minX + width;
        int maxY = minY + height;
        for (WingEntry entry : wings) {
            if (!isActiveWing(entry.wing)) continue;
            if (entry.wing instanceof GuiUnionPanel) {
                Rectangle sub = ((GuiUnionPanel) entry.wing).getUnionBounds();
                minX = Math.min(minX, sub.x);
                minY = Math.min(minY, sub.y);
                maxX = Math.max(maxX, sub.x + sub.width);
                maxY = Math.max(maxY, sub.y + sub.height);
            } else {
                int wx = entry.wing.getAbsoluteX();
                int wy = entry.wing.getAbsoluteY();
                minX = Math.min(minX, wx);
                minY = Math.min(minY, wy);
                maxX = Math.max(maxX, wx + entry.wing.getWidth());
                maxY = Math.max(maxY, wy + entry.wing.getHeight());
            }
        }
        return gfx.isClipped(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    protected boolean isMouseOver(int mouseX, int mouseY) {
        if (super.isMouseOver(mouseX, mouseY)) return true;
        for (WingEntry entry : wings) {
            if (isActiveWing(entry.wing) && entry.wing.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private CornerRadii computeEffectiveMainRadii() {
        CornerRadii r = this.cornerRadii;
        for (WingEntry entry : wings) {
            if (!isActiveWing(entry.wing)) continue;
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
        CornerRadii effMain = computeEffectiveMainRadii().clamp(width, height);

        if ((backgroundColor & 0xFF000000) != 0) {
            gfx.drawRoundedRect(absX, absY, width, height, effMain, backgroundColor);
        }
        for (WingEntry entry : wings) {
            GuiPanel w = entry.wing;
            if (!isActiveWing(w)) continue;
            int wBg = (w.getBackgroundColor() & 0xFF000000) != 0 ? w.getBackgroundColor() : this.backgroundColor;
            if ((wBg & 0xFF000000) != 0) {
                gfx.drawRoundedRect(w.getAbsoluteX(), w.getAbsoluteY(), w.getWidth(), w.getHeight(), computeEffectiveWingRadii(entry), wBg);
            }
        }

        if (borderWidth <= 0 || (borderColor & 0xFF000000) == 0) return;

        int mainSides = BorderSides.ALL;
        for (WingEntry entry : wings) {
            if (isActiveWing(entry.wing)) {
                mainSides = BorderSides.without(mainSides, entry.side.toBorderSide());
            }
        }
        gfx.drawRoundedBorder(absX, absY, width, height, effMain, borderWidth, mainSides, borderColor);

        for (DockSide side : ALL_SIDES) {
            if (BorderSides.has(mainSides, side.toBorderSide())) continue;
            drawUncoveredEdgeSegments(gfx, absX, absY, side, effMain, borderWidth, borderColor);
        }

        for (WingEntry entry : wings) {
            GuiPanel w = entry.wing;
            if (!isActiveWing(w)) continue;
            int wBorderColor = (w.getBorderColor() & 0xFF000000) != 0 ? w.getBorderColor() : this.borderColor;
            int wBorderW = w.getBorderWidth() > 0 ? w.getBorderWidth() : this.borderWidth;
            int wingSides = BorderSides.without(BorderSides.ALL, entry.side.opposite().toBorderSide());
            gfx.drawRoundedBorder(w.getAbsoluteX(), w.getAbsoluteY(), w.getWidth(), w.getHeight(),
                    computeEffectiveWingRadii(entry), wBorderW, wingSides, wBorderColor);
        }
    }

    private void drawUncoveredEdgeSegments(IGraphics gfx, int absX, int absY, DockSide side, CornerRadii effMain, int bw, int color) {
        scratchIntervals.clear();
        int edgeStart;
        int edgeEnd;
        if (side.isHorizontal()) {
            edgeStart = (side == DockSide.LEFT) ? effMain.topLeft : effMain.topRight;
            edgeEnd = height - ((side == DockSide.LEFT) ? effMain.bottomLeft : effMain.bottomRight);
            for (WingEntry entry : wings) {
                if (entry.side == side && isActiveWing(entry.wing)) {
                    int s = Math.max(edgeStart, entry.wing.getY());
                    int e = Math.min(edgeEnd, entry.wing.getY() + entry.wing.getHeight());
                    if (e > s) scratchIntervals.add(new int[] { s, e });
                }
            }
        } else {
            edgeStart = (side == DockSide.TOP) ? effMain.topLeft : effMain.bottomLeft;
            edgeEnd = width - ((side == DockSide.TOP) ? effMain.topRight : effMain.bottomRight);
            for (WingEntry entry : wings) {
                if (entry.side == side && isActiveWing(entry.wing)) {
                    int s = Math.max(edgeStart, entry.wing.getX());
                    int e = Math.min(edgeEnd, entry.wing.getX() + entry.wing.getWidth());
                    if (e > s) scratchIntervals.add(new int[] { s, e });
                }
            }
        }
        if (edgeEnd <= edgeStart) return;
        scratchIntervals.sort(Comparator.comparingInt(a -> a[0]));

        int cursor = edgeStart;
        for (int[] seg : scratchIntervals) {
            if (seg[0] > cursor) {
                drawEdgeSpan(gfx, absX, absY, side, cursor, seg[0], bw, color);
            }
            if (seg[1] > cursor) {
                cursor = seg[1];
            }
        }
        if (cursor < edgeEnd) {
            drawEdgeSpan(gfx, absX, absY, side, cursor, edgeEnd, bw, color);
        }
    }

    private void drawEdgeSpan(IGraphics gfx, int absX, int absY, DockSide side, int start, int end, int bw, int color) {
        int len = end - start;
        if (len <= 0) return;
        switch (side) {
            case LEFT:
                gfx.drawRect(absX, absY + start, bw, len, color);
                break;
            case RIGHT:
                gfx.drawRect(absX + width - bw, absY + start, bw, len, color);
                break;
            case TOP:
                gfx.drawRect(absX + start, absY, len, bw, color);
                break;
            case BOTTOM:
                gfx.drawRect(absX + start, absY + height - bw, len, bw, color);
                break;
        }
    }
}
