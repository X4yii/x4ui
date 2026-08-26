package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.renderer.GlStateManager;
public class GuiButton extends GuiComponent {
    protected String text;
    protected Runnable onClick;
    protected int baseBgColor = 0xFF16161E;
    protected int hoverBgColor = 0xFF2C2C36;
    protected int baseBorderColor = 0xFF2C2C36;
    protected int hoverBorderColor = 0xFFFFFFFF;
    protected int textColor = 0xFFFFFFFF;
    protected float hoverLerp = 0.0f;
    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);
        this.text = text;
        this.onClick = onClick;
    }
    @Override
    public void update() {
        super.update();
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isMouseOver(mouseX, mouseY) && enabled;
        float dt = com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime;
        float speed = 10f * dt;
        if (hovered) {
            hoverLerp = Math.min(1.0f, hoverLerp + speed);
        } else {
            hoverLerp = Math.max(0.0f, hoverLerp - speed);
        }
        int currentBg = lerpColor(baseBgColor, hoverBgColor, hoverLerp);
        int currentBorder = lerpColor(baseBorderColor, hoverBorderColor, hoverLerp);
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawRect(absX, absY, absX + width, absY + height, currentBg);
        drawBorder(absX, absY, absX + width, absY + height, currentBorder);
        int currentTextColor = enabled ? textColor : 0xFF888892;
        drawCenteredString(text, absX + width / 2, absY + (height - 8) / 2, currentTextColor);
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!super.mouseClicked(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && enabled && mouseButton == 0) {
                playClickSound();
                if (onClick != null) {
                    onClick.run();
                }
                return true;
            }
        }
        return false;
    }
    protected void playClickSound() {
        mc.getSoundHandler().playSound(
            net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
            )
        );
    }
    public void setText(String text) { this.text = text; }
    public String getText() { return text; }
    public void setOnClick(Runnable onClick) { this.onClick = onClick; }
    public void setBgColor(int color) { this.baseBgColor = color; }
    public void setHoverBgColor(int color) { this.hoverBgColor = color; }
    public void setTextColor(int color) { this.textColor = color; }
}