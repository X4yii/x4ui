package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.component.GuiPanel;
import com.x4yi.x4ui.common.State;
import org.junit.Assert;
import org.junit.Test;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
public class StateTest {
    @Test
    public void testBasicStateAndListeners() {
        State<String> state = new State<>("hello");
        Assert.assertEquals("hello", state.get());
        AtomicInteger callCount = new AtomicInteger(0);
        state.addListener(val -> callCount.incrementAndGet());
        state.set("world");
        Assert.assertEquals("world", state.get());
        Assert.assertEquals(1, callCount.get());
        state.set("world");
        Assert.assertEquals(1, callCount.get());
        state.set("test");
        Assert.assertEquals(2, callCount.get());
    }
    @Test
    public void testAddListenerWithUnbinder() {
        State<Integer> state = new State<>(0);
        AtomicInteger lastVal = new AtomicInteger(-1);
        Runnable unbind = state.addListenerWithUnbinder(lastVal::set);
        state.set(42);
        Assert.assertEquals(42, lastVal.get());
        unbind.run();
        state.set(99);
        Assert.assertEquals(42, lastVal.get());
    }
    @Test
    public void testMappedStateReactivePropagation() {
        State<Integer> source = new State<>(10);
        State<String> mapped = source.map(v -> "Value: " + v);
        Assert.assertEquals("Value: 10", mapped.get());
        AtomicInteger mappedCalls = new AtomicInteger(0);
        mapped.addListener(v -> mappedCalls.incrementAndGet());
        source.set(20);
        Assert.assertEquals("Value: 20", mapped.get());
        Assert.assertEquals(1, mappedCalls.get());
        source.set(30);
        Assert.assertEquals("Value: 30", mapped.get());
        Assert.assertEquals(2, mappedCalls.get());
    }
    @Test
    public void testWeakListenerDirect() {
        Object target = new Object();
        AtomicInteger called = new AtomicInteger(0);
        State.WeakListener<String> wl = new State.WeakListener<>(target, (t, v) -> called.incrementAndGet());
        Assert.assertFalse(wl.isDead());
        wl.accept("first");
        Assert.assertEquals(1, called.get());
        WeakReference<Object> ref = new WeakReference<>(target);
        target = null;
        for (int i = 0; i < 5 && ref.get() != null; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        if (ref.get() == null) {
            Assert.assertTrue(wl.isDead());
            wl.accept("second");
            Assert.assertEquals(1, called.get());
        }
    }
    @Test
    public void testMappedStateWeakCollection() {
        State<Integer> source = new State<>(1);
        State<String> mapped = source.map(Object::toString);
        WeakReference<State<String>> weakRef = new WeakReference<>(mapped);
        Assert.assertNotNull(weakRef.get());
        Assert.assertEquals("1", mapped.get());
        mapped = null;
        for (int i = 0; i < 5 && weakRef.get() != null; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
        }
        if (weakRef.get() == null) {
            source.set(2);
            Assert.assertEquals((Integer) 2, source.get());
        }
    }
    @Test
    public void testGuiComponentLifecycleWithMapState() {
        GuiPanel panel = new GuiPanel(0, 0, 100, 100);
        State<Integer> source = new State<>(100);
        State<String> mapped = panel.mapState(source, v -> "Val: " + v);
        Assert.assertEquals("Val: 100", mapped.get());
        source.set(200);
        Assert.assertEquals("Val: 200", mapped.get());
        panel.destroy();
        AtomicInteger listenerTriggered = new AtomicInteger(0);
        mapped.addListener(v -> listenerTriggered.incrementAndGet());
        mapped.dispose();
        mapped.set("Custom");
        Assert.assertEquals(0, listenerTriggered.get());
    }
}