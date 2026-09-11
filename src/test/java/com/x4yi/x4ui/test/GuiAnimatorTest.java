package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.animation.GuiAnimator;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class GuiAnimatorTest {
    @Before
    public void setUp() {
        GuiAnimator.clearAnimations();
    }
    @Test
    public void testAnimationLifecycleAndCompletion() throws InterruptedException {
        Object target = new Object();
        AtomicInteger completeCount = new AtomicInteger(0);
        float[] values = new float[1];
        GuiAnimator.animate(target, "testProp", 0.0f, 10.0f, 50, GuiAnimator.Easing.LINEAR, new GuiAnimator.AnimationCallback() {
            @Override
            public void onUpdate(float currentValue) {
                values[0] = currentValue;
            }
            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        });
        Assert.assertEquals(1, GuiAnimator.getActiveAnimationCount());
        GuiAnimator.update();
        Assert.assertTrue(values[0] >= 0.0f);
        Thread.sleep(60);
        GuiAnimator.update();
        Assert.assertEquals(10.0f, values[0], 0.001f);
        Assert.assertEquals(1, completeCount.get());
        Assert.assertEquals(0, GuiAnimator.getActiveAnimationCount());
    }
    @Test
    public void testExplicitCancellation() {
        Object target = new Object();
        GuiAnimator.animate(target, "alpha", 0.0f, 1.0f, 500, GuiAnimator.Easing.EASE_IN, v -> {});
        Assert.assertEquals(1, GuiAnimator.getActiveAnimationCount());
        GuiAnimator.cancel(target, "alpha");
        Assert.assertEquals(0, GuiAnimator.getActiveAnimationCount());
        GuiAnimator.animate(target, "x", 0.0f, 100.0f, 500, GuiAnimator.Easing.LINEAR, v -> {});
        GuiAnimator.animate(target, "y", 0.0f, 100.0f, 500, GuiAnimator.Easing.LINEAR, v -> {});
        Assert.assertEquals(2, GuiAnimator.getActiveAnimationCount());
        GuiAnimator.cancel(target);
        Assert.assertEquals(0, GuiAnimator.getActiveAnimationCount());
    }
    @Test
    public void testWeakTargetAutoPruningOnGC() {
        Object target = new Object();
        WeakReference<Object> ref = new WeakReference<>(target);
        AtomicBoolean updated = new AtomicBoolean(false);
        GuiAnimator.animate(target, "scale", 1.0f, 2.0f, 1000, GuiAnimator.Easing.LINEAR, v -> updated.set(true));
        Assert.assertEquals(1, GuiAnimator.getActiveAnimationCount());
        target = null;
        for (int i = 0; i < 5 && ref.get() != null; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        if (ref.get() == null) {
            GuiAnimator.update();
            Assert.assertEquals(0, GuiAnimator.getActiveAnimationCount());
        }
    }
    @Test
    public void testExceptionIsolationInCallback() {
        Object target = new Object();
        GuiAnimator.animate(target, "faulty", 0.0f, 1.0f, 1000, GuiAnimator.Easing.LINEAR, v -> {
            throw new RuntimeException("Simulated callback failure");
        });
        Assert.assertEquals(1, GuiAnimator.getActiveAnimationCount());
        GuiAnimator.update();
        Assert.assertEquals(0, GuiAnimator.getActiveAnimationCount());
    }
    @Test
    public void testComponentDestroyCancelsActiveAnimations() {
        GuiPanel panel = new GuiPanel(0, 0, 100, 100);
        GuiAnimator.animate(panel, "opacity", 0.0f, 1.0f, 1000, GuiAnimator.Easing.LINEAR, v -> {});
        Assert.assertEquals(1, GuiAnimator.getActiveAnimationCount());
        panel.destroy();
        Assert.assertEquals(0, GuiAnimator.getActiveAnimationCount());
    }
}