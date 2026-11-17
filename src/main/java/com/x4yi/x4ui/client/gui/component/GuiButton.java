package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.renderer.GlStateManager;
public class GuiButton extends GuiComponent {
    public enum Variant {
        DEFAULT, PRIMARY, SECONDARY, SUCCESS, DANGER, GHOST
    }
    protected String text;
    protected Runnable onClick;
    protected float hoverLerp = 0.0f;
    protected Variant variant = Variant.DEFAULT;
    protected boolean selected = false;
    public GuiButton(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);
        this.text = text;
        this.onClick = onClick;
    }
    public static GuiButton of(String text, Runnable onClick) {
        return new GuiButton(0, 0, 100, 22, text, onClick);
    }
    public GuiButton withVariant(Variant variant) { this.variant = variant; return this; }
    public GuiButton withSelected(boolean selected) { this.selected = selected; return this; }
    public void setSelected(boolean selected) { this.selected = selected; }
    public boolean isSelected() { return selected; }
    protected float lastDeltaTime = 0.016f;
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (deltaTime > 0.0f) {
            this.lastDeltaTime = deltaTime;
        }
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        boolean hovered = isMouseOver(mouseX, mouseY) && enabled;
        float dt = lastDeltaTime > 0.0f ? lastDeltaTime : 0.016f;
        float speed = Math.min(1.0f, 10f * dt);
        if (hovered) {
            hoverLerp = Math.min(1.0f, hoverLerp + speed);
        } else {
            hoverLerp = Math.max(0.0f, hoverLerp - speed);
        }
        com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
        int baseBg, baseHoverBg, idleBorder, hoverBorder;
        int primaryCol = theme.getPrimaryColor();
        switch (variant) {
            case PRIMARY:
                baseBg = theme.getButtonPrimaryColor();
                baseHoverBg = brightenColor(baseBg, 0.14f);
                idleBorder = baseBg;
                hoverBorder = baseHoverBg;
                break;
            case SECONDARY:
                baseBg = theme.getButtonSecondaryColor();
                baseHoverBg = brightenColor(baseBg, 0.14f);
                idleBorder = baseBg;
                hoverBorder = baseHoverBg;
                break;
            case SUCCESS:
                baseBg = theme.getButtonSuccessColor();
                baseHoverBg = brightenColor(baseBg, 0.14f);
                idleBorder = baseBg;
                hoverBorder = baseHoverBg;
                break;
            case DANGER:
                baseBg = theme.getButtonDangerColor();
                baseHoverBg = brightenColor(baseBg, 0.14f);
                idleBorder = baseBg;
                hoverBorder = baseHoverBg;
                break;
            case GHOST:
                baseBg = 0x00000000;
                baseHoverBg = theme.getButtonHoverColor();
                idleBorder = 0x00000000;
                hoverBorder = theme.getCardBorderColor();
                break;
            case DEFAULT:
            default:
                baseBg = theme.getButtonBackgroundColor();
                baseHoverBg = theme.getButtonHoverColor();
                idleBorder = theme.getCardBorderColor();
                hoverBorder = primaryCol;
                break;
        }
        if (selected) {
            baseBg = (variant == Variant.GHOST) ? theme.getButtonHoverColor() : primaryCol;
            baseHoverBg = brightenColor(baseBg, 0.14f);
            idleBorder = primaryCol;
            hoverBorder = primaryCol;
        }
        if (!enabled) {
            baseBg = theme.getButtonDisabledColor();
            baseHoverBg = baseBg;
            idleBorder = theme.getCardBorderColor();
            hoverBorder = idleBorder;
        }
        int currentBg = lerpColor(baseBg, baseHoverBg, hoverLerp);
        int currentBorder = lerpColor(idleBorder, hoverBorder, hoverLerp);
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        gfx.drawRect(absX, absY, width, height, currentBg);
        gfx.drawBorder(absX, absY, width, height, 1, currentBorder);
        if (selected && variant == Variant.GHOST) {
            gfx.drawRoundedRect(absX + 2, absY + 3, 3, height - 6, 1, primaryCol);
        }
        int currentTextColor = enabled ? (selected ? 0xFFFFFFFF : theme.getTextColor()) : theme.getDisabledTextColor();
        if (text != null && !text.isEmpty()) {
            int textX = absX + width / 2;
            if (variant == Variant.GHOST) {
                textX = absX + (selected ? 9 : 6) + gfx.getStringWidth(text) / 2;
            }
            gfx.drawStringCentered(text, textX, absY + (height - gfx.getFontHeight()) / 2.0f, currentTextColor, false);
        }
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled) return false;
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
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
        if (mc != null && mc.getSoundHandler() != null) {
            mc.getSoundHandler().playSound(
                net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                    net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
                )
            );
        }
    }
    private static int brightenColor(int color, float amount) {
        int a = (color >>> 24) & 0xFF;
        int r = (color >>> 16) & 0xFF;
        int g = (color >>> 8) & 0xFF;
        int b = color & 0xFF;
        r = Math.min(255, (int) (r + (255 - r) * amount));
        g = Math.min(255, (int) (g + (255 - g) * amount));
        b = Math.min(255, (int) (b + (255 - b) * amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void setText(String text) { this.text = text; }
    public String getText() { return text; }
    public void setOnClick(Runnable onClick) { this.onClick = onClick; }
}