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
    @Override
    public void update() {
        super.update();
        if (slot != null && containerScreen != null) {
            int absX = getAbsoluteX();
            int absY = getAbsoluteY();
            slot.xPos = absX - containerScreen.getGuiLeft();
            slot.yPos = absY - containerScreen.getGuiTop();
        }
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        drawRect(absX, absY, absX + width, absY + height, 0x55000000);
        drawBorder(absX, absY, absX + width, absY + height, 0xFF333333);
    }
}