package com.x4yi.x4ui.client.gui.component.toast;
@FunctionalInterface
public interface ToastAnimation {
    float getProgress(float time, float duration);
    ToastAnimation LINEAR = (t, d) -> Math.max(0.0f, Math.min(1.0f, t / d));
    ToastAnimation EASE_OUT_CUBIC = (t, d) -> {
        float f = Math.max(0.0f, Math.min(1.0f, t / d)) - 1.0f;
        return f * f * f + 1.0f;
    };
    ToastAnimation EASE_IN_CUBIC = (t, d) -> {
        float f = Math.max(0.0f, Math.min(1.0f, t / d));
        return f * f * f;
    };
    ToastAnimation EASE_IN_OUT_CUBIC = (t, d) -> {
        float f = Math.max(0.0f, Math.min(1.0f, t / d));
        return f < 0.5f ? 4.0f * f * f * f : 1.0f - (float) Math.pow(-2.0f * f + 2.0f, 3.0) / 2.0f;
    };
    ToastAnimation EASE_OUT_BACK = easeOutBack(1.70158f);
    static ToastAnimation easeOutBack(float overshoot) {
        return (t, d) -> {
            float f = Math.max(0.0f, Math.min(1.0f, t / d)) - 1.0f;
            return 1.0f + (overshoot + 1.0f) * (float) Math.pow(f, 3) + overshoot * (float) Math.pow(f, 2);
        };
    }
}