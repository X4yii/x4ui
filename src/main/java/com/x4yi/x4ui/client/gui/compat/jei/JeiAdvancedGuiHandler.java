package com.x4yi.x4ui.client.gui.compat.jei;
import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiSlot;
import com.x4yi.x4ui.client.gui.overlay.GuiOverlayManager;
import com.x4yi.x4ui.client.gui.overlay.IGuiOverlay;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import javax.annotation.Nullable;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
public class JeiAdvancedGuiHandler implements IAdvancedGuiHandler<GuiContainer> {
    @Override
    public Class<GuiContainer> getGuiContainerClass() {
        return GuiContainer.class;
    }
    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(GuiContainer guiContainer) {
        List<Rectangle> areas = new ArrayList<>();
        if (guiContainer instanceof GuiBaseContainer) {
            GuiBaseContainer baseContainer = (GuiBaseContainer) guiContainer;
            List<Rectangle> jeiAreas = baseContainer.getJeiRecipeAreas();
            if (jeiAreas != null && !jeiAreas.isEmpty()) {
                areas.addAll(jeiAreas);
            }
        }
        List<Rectangle> overlayAreas = GuiOverlayManager.INSTANCE.getExclusionAreas(guiContainer);
        if (overlayAreas != null && !overlayAreas.isEmpty()) {
            areas.addAll(overlayAreas);
        }
        return areas.isEmpty() ? null : areas;
    }
    @Nullable
    @Override
    public Object getIngredientUnderMouse(GuiContainer guiContainer, int mouseX, int mouseY) {
        if (guiContainer instanceof GuiBaseContainer) {
            GuiBaseContainer base = (GuiBaseContainer) guiContainer;
            GuiComponent root = base.getRootPanel();
            if (root != null) {
                GuiComponent hovered = root.getHoveredComponent(mouseX, mouseY);
                if (hovered instanceof GuiSlot) {
                    GuiSlot slot = (GuiSlot) hovered;
                    if (slot.getSlot() != null && slot.getSlot().getHasStack()) {
                        return slot.getSlot().getStack();
                    }
                }
            }
        }
        for (IGuiOverlay overlay : GuiOverlayManager.INSTANCE.getActiveOverlays(guiContainer)) {
            GuiComponent root = overlay.getRootComponent();
            if (root != null && root.isVisible() && root.contains(mouseX, mouseY)) {
                GuiComponent hovered = root.getHoveredComponent(mouseX, mouseY);
                if (hovered instanceof GuiSlot) {
                    GuiSlot slot = (GuiSlot) hovered;
                    if (slot.getSlot() != null && slot.getSlot().getHasStack()) {
                        return slot.getSlot().getStack();
                    }
                }
            }
        }
        return null;
    }
}