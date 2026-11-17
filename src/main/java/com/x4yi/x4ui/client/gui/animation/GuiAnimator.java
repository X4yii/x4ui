package com.x4yi.x4ui.client.gui.animation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class GuiAnimator {
    public enum Easing {
        LINEAR,
        EASE_IN,
        EASE_OUT,
        EASE_IN_OUT
    }
    private static class AnimationTask {
        private final WeakReference<Object> targetRef;
        private final String property;
        private final float startValue;
        private final float endValue;
        private final long startTime;
        private final long durationMs;
        private final Easing easing;
        private final AnimationCallback callback;
        public AnimationTask(Object target, String property, float startValue, float endValue, long durationMs, Easing easing, AnimationCallback callback) {
            this.targetRef = new WeakReference<>(target);
            this.property = property != null ? property : "";
            this.startValue = startValue;
            this.endValue = endValue;
            this.startTime = System.currentTimeMillis();
            this.durationMs = Math.max(1L, durationMs);
            this.easing = easing != null ? easing : Easing.LINEAR;
            this.callback = callback;
        }
        public Object getTarget() {
            return targetRef.get();
        }
        public boolean isDead() {
            return targetRef.get() == null;
        }
    }
    public interface AnimationCallback {
        void onUpdate(float currentValue);
        default void onComplete() {}
    }
    private static final Logger LOGGER = LogManager.getLogger("x4ui");
    private static final List<AnimationTask> activeAnimations = new ArrayList<>();
    public static void animate(Object target, String property, float startValue, float endValue, long durationMs, Easing easing, AnimationCallback callback) {
        if (target == null || callback == null) return;
        synchronized (activeAnimations) {
            activeAnimations.removeIf(task -> {
                Object t = task.getTarget();
                return t == null || (t == target && task.property.equals(property));
            });
            activeAnimations.add(new AnimationTask(target, property, startValue, endValue, durationMs, easing, callback));
        }
    }
    public static void cancel(Object target) {
        if (target == null) return;
        synchronized (activeAnimations) {
            activeAnimations.removeIf(task -> {
                Object t = task.getTarget();
                return t == null || t == target;
            });
        }
    }
    public static void cancel(Object target, String property) {
        if (target == null || property == null) return;
        synchronized (activeAnimations) {
            activeAnimations.removeIf(task -> {
                Object t = task.getTarget();
                return t == null || (t == target && Objects.equals(task.property, property));
            });
        }
    }
    public static int getActiveAnimationCount() {
        synchronized (activeAnimations) {
            return activeAnimations.size();
        }
    }
    public static void update() {
        List<AnimationTask> snapshot;
        synchronized (activeAnimations) {
            if (activeAnimations.isEmpty()) return;
            snapshot = new ArrayList<>(activeAnimations);
        }
        long currentTime = System.currentTimeMillis();
        List<AnimationTask> toRemove = null;
        for (AnimationTask task : snapshot) {
            Object target = task.getTarget();
            if (target == null) {
                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(task);
                continue;
            }
            float progress = (float) (currentTime - task.startTime) / task.durationMs;
            if (progress >= 1.0f) {
                try {
                    task.callback.onUpdate(task.endValue);
                    task.callback.onComplete();
                } catch (Exception e) {
                    LOGGER.warn("Animation completion callback failed for property '{}'", task.property, e);
                }
                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(task);
            } else {
                float easedProgress = applyEasing(progress, task.easing);
                float currentValue = task.startValue + (task.endValue - task.startValue) * easedProgress;
                try {
                    task.callback.onUpdate(currentValue);
                } catch (Exception e) {
                    LOGGER.warn("Animation update callback failed for property '{}'", task.property, e);
                    if (toRemove == null) toRemove = new ArrayList<>();
                    toRemove.add(task);
                }
            }
        }
        if (toRemove != null) {
            synchronized (activeAnimations) {
                activeAnimations.removeAll(toRemove);
            }
        }
    }
    public static void clearAnimations() {
        synchronized (activeAnimations) {
            activeAnimations.clear();
        }
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