package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.base.GuiBaseContainer;
import net.minecraft.inventory.Slot;

public class GuiSlot extends GuiComponent {
    private final Slot slot;
    private final GuiBaseContainer containerScreen;

    public GuiSlot(Slot slot, GuiBaseContainer containerScreen) {
        super(0, 0, 18, 18);
        this.slot = slot;
        this.containerScreen = containerScreen;
    }

    public static GuiSlot of(Slot slot, GuiBaseContainer containerScreen) {
        return new GuiSlot(slot, containerScreen);
    }

    public Slot getSlot() {
        return slot;
    }

    @Override
    protected void onEffectiveVisibilityChanged(boolean effectivelyVisible) {
        super.onEffectiveVisibilityChanged(effectivelyVisible);
        if (!effectivelyVisible && slot != null) {
            slot.xPos = -9999;
            slot.yPos = -9999;
        }
    }

    @Override
    protected void onDetached() {
        super.onDetached();
        if (slot != null) {
            slot.xPos = -9999;
            slot.yPos = -9999;
        }
    }

    @Override
    protected void onBoundsChanged() {
        syncSlotPosition();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        syncSlotPosition();
    }

    private void syncSlotPosition() {
        if (slot == null || containerScreen == null) return;
        if (!isEffectivelyVisible() || isClippedByAncestors()) {
            slot.xPos = -9999;
            slot.yPos = -9999;
        } else {
            int absX = getAbsoluteX();
            int absY = getAbsoluteY();
            slot.xPos = absX - containerScreen.getGuiLeft() + 1;
            slot.yPos = absY - containerScreen.getGuiTop() + 1;
        }
    }

    private boolean isClippedByAncestors() {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        GuiComponent current = this.parent;
        while (current != null) {
            if (current instanceof GuiScrollPanel) {
                int pX = current.getAbsoluteX();
                int pY = current.getAbsoluteY();
                int pW = current.getWidth();
                int pH = current.getHeight();
                if (absX < pX || absX + width > pX + pW || absY < pY || absY + height > pY + pH) {
                    return true;
                }
            }
            current = current.getParent();
        }
        return false;
    }

    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        gfx.drawRect(absX, absY, width, height, 0x55000000);
        gfx.drawBorder(absX, absY, width, height, 1, 0xFF333333);
    }
}
