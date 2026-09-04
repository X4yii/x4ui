package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.renderer.GlStateManager;
public class GuiButton extends GuiComponent {
    protected String text;
    protected Runnable onClick;
    protected float hoverLerp = 0.0f;
    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);
        this.text = text;
        this.onClick = onClick;
    }
    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isMouseOver(mouseX, mouseY) && enabled;
        float dt = com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime;
        float speed = Math.min(1.0f, 10f * dt);
        if (hovered) {
            hoverLerp = Math.min(1.0f, hoverLerp + speed);
        } else {
            hoverLerp = Math.max(0.0f, hoverLerp - speed);
        }
        com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
        int currentBg = lerpColor(theme.getButtonBackgroundColor(), theme.getButtonHoverColor(), hoverLerp);
        int currentBorder = lerpColor(theme.getBackgroundColor(), theme.getPrimaryColor(), hoverLerp);
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawRect(absX, absY, absX + width, absY + height, currentBg);
        drawBorder(absX, absY, absX + width, absY + height, currentBorder);
        int currentTextColor = enabled ? theme.getTextColor() : theme.getDisabledTextColor();
        drawCenteredString(text, absX + width / 2, absY + (height - fontRenderer.FONT_HEIGHT) / 2, currentTextColor);
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
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
}