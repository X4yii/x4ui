package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.utils.ITheme;
import com.x4yi.x4ui.common.State;
import java.util.function.Consumer;
public class GuiToggle extends GuiComponent {
    protected boolean state;
    protected State<Boolean> boundState;
    protected Consumer<Boolean> onStateChanged;
    protected String label;
    protected float animationLerp = 0.0f;
    public GuiToggle(int x, int y, int width, int height, String label, boolean defaultState, Consumer<Boolean> onStateChanged) {
        super(x, y, width, height);
        this.label = label;
        this.state = defaultState;
        this.onStateChanged = onStateChanged;
        this.animationLerp = state ? 1.0f : 0.0f;
    }
    public GuiToggle(int x, int y, int width, int height, String label, State<Boolean> state) {
        this(x, y, width, height, label, state != null ? state.get() : false, null);
        if (state != null) {
            bindState(state);
        }
    }
    public GuiToggle bindState(State<Boolean> state) {
        this.boundState = state;
        this.state = state != null ? state.get() : false;
        this.animationLerp = this.state ? 1.0f : 0.0f;
        if (state != null) {
            bindState(state, val -> {
                this.state = val != null ? val : false;
            });
        }
        return this;
    }
    public boolean getState() { return state; }
    public void setState(boolean state) {
        if (this.state != state) {
            this.state = state;
            if (boundState != null) {
                boundState.set(state);
            }
            if (onStateChanged != null) {
                onStateChanged.accept(state);
            }
        }
    }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float target = state ? 1.0f : 0.0f;
        float speed = Math.min(1.0f, 15f * deltaTime);
        animationLerp += (target - animationLerp) * speed;
        if (Math.abs(target - animationLerp) < 0.01f) {
            animationLerp = target;
        }
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        ITheme theme = getTheme();
        int labelColor = (isMouseOver(mouseX, mouseY) && enabled) ? theme.getTextColor() : theme.getDisabledTextColor();
        int fontHeight = gfx.getFontHeight();
        if (label != null && !label.isEmpty()) {
            gfx.drawString(label, absX, absY + (height - fontHeight) / 2.0f, labelColor, false);
        }
        int badgeH = Math.max(12, fontHeight + 3);
        int badgeW = Math.max(26, Math.max(gfx.getStringWidth("ON"), gfx.getStringWidth("OFF")) + 8);
        int badgeX = absX + width - badgeW;
        int badgeY = absY + (height - badgeH) / 2;
        int colorOff = theme.getToggleOffColor();
        int colorOn = theme.getToggleOnColor();
        int currentColor = lerpColor(colorOff, colorOn, animationLerp);
        int radius = Math.min(badgeH / 2, 4);
        gfx.drawRoundedRect(badgeX, badgeY, badgeW, badgeH, radius, currentColor);
        String text = state ? "ON" : "OFF";
        int tColor = lerpColor(theme.getDisabledTextColor(), theme.getTextColor(), animationLerp);
        gfx.drawStringCentered(text, badgeX + badgeW / 2.0f, badgeY + (badgeH - fontHeight) / 2.0f + 1, tColor, false);
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && enabled && mouseButton == 0) {
                if (mc != null && mc.getSoundHandler() != null) {
                    mc.getSoundHandler().playSound(
                        net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                            net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
                        )
                    );
                }
                setState(!state);
                return true;
            }
        }
        return false;
    }
}