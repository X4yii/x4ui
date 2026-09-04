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
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        float target = state ? 1.0f : 0.0f;
        float speed = Math.min(1.0f, 15f * deltaTime);
        animationLerp += (target - animationLerp) * speed;
        if (Math.abs(target - animationLerp) < 0.01f) {
            animationLerp = target;
        }
    }

    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        ITheme theme = getTheme();

        int labelColor = (isMouseOver(mouseX, mouseY) && enabled) ? theme.getTextColor() : theme.getDisabledTextColor();
        drawString(label, absX, absY + (height - fontRenderer.FONT_HEIGHT) / 2, labelColor);

        int badgeW = 24;
        int badgeH = 12;
        int badgeX = absX + width - badgeW;
        int badgeY = absY + (height - badgeH) / 2;

        int colorOff = theme.getToggleOffColor();
        int colorOn = theme.getToggleOnColor();
        int currentColor = lerpColor(colorOff, colorOn, animationLerp);

        drawRect(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH, currentColor);

        String text = state ? "ON" : "OFF";
        int tColor = lerpColor(theme.getDisabledTextColor(), theme.getTextColor(), animationLerp);
        drawCenteredString(text, badgeX + badgeW / 2, badgeY + (badgeH - fontRenderer.FONT_HEIGHT) / 2 + 1, tColor);
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!super.onMouseClick(mouseX, mouseY, mouseButton)) {
            if (isMouseOver(mouseX, mouseY) && enabled && mouseButton == 0) {
                mc.getSoundHandler().playSound(
                    net.minecraft.client.audio.PositionedSoundRecord.getMasterRecord(
                        net.minecraft.init.SoundEvents.UI_BUTTON_CLICK, 1.0F
                    )
                );
                setState(!state);
                return true;
            }
        }
        return false;
    }
}