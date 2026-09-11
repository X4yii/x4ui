package com.x4yi.x4ui.client.gui.overlay;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import net.minecraft.client.gui.GuiScreen;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
public interface IGuiOverlay {
    String getId();
    default int getPriority() {
        return 0;
    }
    boolean matches(GuiScreen screen);
    void init(GuiScreen screen, OverlayContext context);
    default void update(GuiScreen screen, float deltaTime) {
        GuiComponent root = getRootComponent();
        if (root != null) {
            root.update(deltaTime);
        }
    }
    default void renderBackground(GuiScreen screen, IGraphics gfx, int mouseX, int mouseY, float partialTicks) {}
    default void renderForeground(GuiScreen screen, IGraphics gfx, int mouseX, int mouseY, float partialTicks) {}
    GuiComponent getRootComponent();
    default ContainerAnchor getAnchor() {
        return ContainerAnchor.CONTAINER_RIGHT;
    }
    default int getOffsetX() {
        return 4;
    }
    default int getOffsetY() {
        return 0;
    }
    default int getStackSpacing() {
        return 4;
    }
    default List<Rectangle> getExclusionAreas() {
        GuiComponent root = getRootComponent();
        if (root != null && root.isVisible()) {
            return Collections.singletonList(new Rectangle(root.getX(), root.getY(), root.getWidth(), root.getHeight()));
        }
        return Collections.emptyList();
    }
    default void onClosed(GuiScreen screen) {
        GuiComponent root = getRootComponent();
        if (root != null) {
            root.destroy();
        }
    }
}