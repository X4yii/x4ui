package com.x4yi.x4ui.client.gui.component;
import java.util.function.Consumer;
public class GuiToggle extends GuiComponent {
    protected boolean state;
    protected Consumer<Boolean> onStateChanged;
    protected String label;
    protected float animationLerp = 0.0f;
    protected int enabledColor = 0x4CAF50;
    protected int disabledColor = 0x666666;
    protected int textColor = 0xFFFFFF;
    public GuiToggle(int x, int y, int width, int height, String label, boolean defaultState, Consumer<Boolean> onStateChanged) {
        super(x, y, width, height);
        this.label = label;
        this.state = defaultState;
        this.onStateChanged = onStateChanged;
        this.animationLerp = state ? 1.0f : 0.0f;
    }
    public boolean getState() { return state; }
    public void setState(boolean state) { this.state = state; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        float target = state ? 1.0f : 0.0f;
        animationLerp += (target - animationLerp) * 0.2f;
        if (Math.abs(target - animationLerp) < 0.01f) {
            animationLerp = target;
        }
        int labelColor = (isMouseOver(mouseX, mouseY) && enabled) ? 0xFFFFFFFF : 0xFFB0B0BB;
        drawString(label, absX, absY + (height - 8) / 2, labelColor);
        int badgeW = 24;
        int badgeH = 12;
        int badgeX = absX + width - badgeW;
        int badgeY = absY + (height - badgeH) / 2;
        int colorOff = 0xFF424248;
        int colorOn = 0xFF00C853;
        int currentColor = lerpColor(colorOff, colorOn, animationLerp);
        drawRect(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, currentColor);
        String text = state ? "ON" : "OFF";
        int tColor = lerpColor(0xFFA0A0A5, 0xFFFFFFFF, animationLerp);
        drawCenteredString(text, badgeX + badgeW / 2, badgeY + 2, tColor);
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!super.mouseClicked(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && enabled && mouseButton == 0) {
                mc.getSoundHandler().playSound(
                    net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                        net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
                    )
                );
                state = !state;
                if (onStateChanged != null) onStateChanged.accept(state);
                return true;
            }
        }
        return false;
    }
}