package com.x4yi.x4ui.client.gui.component;
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
    public Slot getSlot() {
        return slot;
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (slot != null && containerScreen != null) {
            if (isClippedByAncestors()) {
                slot.xPos = -9999;
                slot.yPos = -9999;
            } else {
                int absX = getAbsoluteX();
                int absY = getAbsoluteY();
                slot.xPos = absX - containerScreen.getGuiLeft();
                slot.yPos = absY - containerScreen.getGuiTop();
            }
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
                if (absX + width <= pX || absX >= pX + pW || absY + height <= pY || absY >= pY + pH) {
                    return true;
                }
            }
            current = current.getParent();
        }
        return false;
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        gfx.drawRect(absX, absY, width, height, 0x55000000);
        gfx.drawBorder(absX, absY, width, height, 1, 0xFF333333);
    }
}