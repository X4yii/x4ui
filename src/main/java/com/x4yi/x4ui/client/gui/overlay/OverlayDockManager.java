package com.x4yi.x4ui.client.gui.overlay;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
public final class OverlayDockManager {
    private OverlayDockManager() {}
    public static void layout(List<IGuiOverlay> overlays, OverlayContext context) {
        if (overlays == null || overlays.isEmpty() || context == null) {
            return;
        }
        Map<ContainerAnchor, List<IGuiOverlay>> grouped = new EnumMap<>(ContainerAnchor.class);
        for (IGuiOverlay overlay : overlays) {
            if (overlay == null) continue;
            ContainerAnchor anchor = overlay.getAnchor();
            if (anchor == null) anchor = ContainerAnchor.CONTAINER_RIGHT;
            if (anchor == ContainerAnchor.FULLSCREEN) continue;
            grouped.computeIfAbsent(anchor, k -> new ArrayList<>()).add(overlay);
        }
        for (Map.Entry<ContainerAnchor, List<IGuiOverlay>> entry : grouped.entrySet()) {
            ContainerAnchor anchor = entry.getKey();
            List<IGuiOverlay> list = entry.getValue();
            list.sort(Comparator.comparingInt(IGuiOverlay::getPriority).reversed());
            int stackOffset = 0;
            for (IGuiOverlay overlay : list) {
                GuiComponent root = overlay.getRootComponent();
                if (root == null || !root.isVisible()) {
                    continue;
                }
                Point pt = anchor.calculate(
                        context.getGuiLeft(), context.getGuiTop(),
                        context.getXSize(), context.getYSize(),
                        context.getScreenWidth(), context.getScreenHeight(),
                        root.getWidth(), root.getHeight(),
                        overlay.getOffsetX(), overlay.getOffsetY(),
                        stackOffset
                );
                root.setX(pt.x);
                root.setY(pt.y);
                root.requestLayout();
                if (anchor == ContainerAnchor.CONTAINER_TOP || anchor == ContainerAnchor.CONTAINER_BOTTOM) {
                    stackOffset += root.getWidth() + overlay.getStackSpacing();
                } else {
                    stackOffset += root.getHeight() + overlay.getStackSpacing();
                }
            }
        }
    }
}