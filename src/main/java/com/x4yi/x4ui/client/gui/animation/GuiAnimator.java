package com.x4yi.x4ui.client.gui.animation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class GuiAnimator {
    public enum Easing {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT
    }
    private static class AnimationTask {
        public Object target;
        public String property;
        public float startValue;
        public float endValue;
        public long startTime;
        public long durationMs;
        public Easing easing;
        public AnimationCallback callback;
        public AnimationTask(Object target, String property, float startValue, float endValue, long durationMs, Easing easing, AnimationCallback callback) {
            this.target = target;
            this.property = property;
            this.startValue = startValue;
            this.endValue = endValue;
            this.startTime = System.currentTimeMillis();
            this.durationMs = durationMs;
            this.easing = easing;
            this.callback = callback;
        }
    }
    public interface AnimationCallback {
        void onUpdate(float currentValue);
        default void onComplete() {}
    }
    private static final List<AnimationTask> activeAnimations = new ArrayList<>();
    public static void animate(Object target, String property, float startValue, float endValue, long durationMs, Easing easing, AnimationCallback callback) {
        activeAnimations.removeIf(task -> task.target == target && task.property.equals(property));
        activeAnimations.add(new AnimationTask(target, property, startValue, endValue, durationMs, easing, callback));
    }
    public static void update() {
        long currentTime = System.currentTimeMillis();
        Iterator<AnimationTask> iterator = activeAnimations.iterator();
        while (iterator.hasNext()) {
            AnimationTask task = iterator.next();
            float progress = (float) (currentTime - task.startTime) / task.durationMs;
            if (progress >= 1.0f) {
                task.callback.onUpdate(task.endValue);
                task.callback.onComplete();
                iterator.remove();
            } else {
                float easedProgress = applyEasing(progress, task.easing);
                float currentValue = task.startValue + (task.endValue - task.startValue) * easedProgress;
                task.callback.onUpdate(currentValue);
            }
        }
    }
    public static void clearAnimations() {
        activeAnimations.clear();
    }
    private static float applyEasing(float t, Easing easing) {
        switch (easing) {
            case EASE_IN:
                return t * t;
            case EASE_OUT:
                return t * (2 - t);
            case EASE_IN_OUT:
                return t < .5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
            case LINEAR:
            default:
                return t;
        }
    }
}