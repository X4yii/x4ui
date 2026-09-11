package com.x4yi.x4ui.client.gui.component.toast;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import net.minecraft.client.renderer.GlStateManager;
public class GuiToast extends GuiPanel {
    protected float duration = 5.0f;
    protected float aliveTime = 0.0f;
    protected ToastAnchor anchor = ToastAnchor.TOP_RIGHT;
    protected ToastAnimation inAnimation = ToastAnimation.EASE_OUT_BACK;
    protected ToastAnimation outAnimation = ToastAnimation.EASE_IN_CUBIC;
    protected float animTime = 0.0f;
    protected float inDuration = 0.5f;
    protected float outDuration = 0.3f;
    protected boolean exiting = false;
    protected Runnable onDismiss;
    public int targetX;
    public int targetY;
    public float currentBaseX = Float.NaN;
    public float currentBaseY = Float.NaN;
    public GuiToast(int width, int height) {
        super(0, 0, width, height);
        this.setCornerRadius(6);
        this.setBorderWidth(1);
        this.setBackgroundColor(0xEE121420);
        this.setBorderColor(0xFF2A2D3D);
    }
    @Override
    protected void onAttached(GuiComponent parent) {
        super.onAttached(parent);
        com.x4yi.x4ui.client.gui.utils.ITheme theme = getTheme();
        this.setBackgroundColor(theme.getCardBackgroundColor());
        this.setBorderColor(theme.getCardBorderColor());
    }
    public GuiToast withDuration(float seconds) { this.duration = seconds; return this; }
    public GuiToast withAnchor(ToastAnchor anchor) { this.anchor = anchor; return this; }
    public GuiToast withInAnimation(ToastAnimation anim, float durationSecs) {
        this.inAnimation = anim;
        this.inDuration = durationSecs;
        return this;
    }
    public GuiToast withOutAnimation(ToastAnimation anim, float durationSecs) {
        this.outAnimation = anim;
        this.outDuration = durationSecs;
        return this;
    }
    public GuiToast onDismiss(Runnable callback) { this.onDismiss = callback; return this; }
    public void dismiss() {
        if (!exiting) {
            exiting = true;
            animTime = 0.0f;
        }
    }
    public boolean isDead() {
        return exiting && animTime >= outDuration;
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        animTime += deltaTime;
        if (!exiting) {
            aliveTime += deltaTime;
            if (duration > 0 && aliveTime >= duration) {
                dismiss();
            }
        }
    }
    public void addAliveTime(float dt) {
        if (!exiting) {
            aliveTime += dt;
            if (duration > 0 && aliveTime >= duration) {
                dismiss();
            }
        }
    }
    public float getSlideProgress() {
        if (exiting) {
            return 1.0f - outAnimation.getProgress(animTime, outDuration);
        } else {
            return inAnimation.getProgress(animTime, inDuration);
        }
    }
    public void setTargetPosition(int x, int y) {
        this.targetX = x;
        this.targetY = y;
    }
    public ToastAnchor getAnchor() {
        return anchor;
    }
    public void notifyDismissed() {
        if (onDismiss != null) {
            onDismiss.run();
        }
    }
}