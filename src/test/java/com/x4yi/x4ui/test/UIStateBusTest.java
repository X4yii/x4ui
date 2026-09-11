package com.x4yi.x4ui.test;
import com.x4yi.x4ui.client.gui.bus.UIStateBus;
import com.x4yi.x4ui.common.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;
public class UIStateBusTest {
    @Before
    public void setUp() {
        UIStateBus.clear();
    }
    @Test
    public void testRegisterAndGet() {
        State<String> state = new State<>("init");
        UIStateBus.register("mod:test_str", state);
        Assert.assertTrue(UIStateBus.has("mod:test_str"));
        State<String> retrieved = UIStateBus.get("mod:test_str", String.class);
        Assert.assertNotNull(retrieved);
        Assert.assertEquals("init", retrieved.get());
    }
    @Test
    public void testGetOrCreate() {
        State<Integer> mana = UIStateBus.getOrCreate("magicmod:mana", 100);
        Assert.assertEquals(Integer.valueOf(100), mana.get());
        State<Integer> same = UIStateBus.getOrCreate("magicmod:mana", 50);
        Assert.assertSame(mana, same);
        Assert.assertEquals(Integer.valueOf(100), same.get());
    }
    @Test
    public void testReactiveInterModSync() {
        State<Integer> sharedCounter = UIStateBus.getOrCreate("bus:counter", 0);
        AtomicInteger listenerUpdates = new AtomicInteger(0);
        sharedCounter.addListener(val -> listenerUpdates.incrementAndGet());
        sharedCounter.set(10);
        Assert.assertEquals(1, listenerUpdates.get());
        Assert.assertEquals(Integer.valueOf(10), sharedCounter.get());
        sharedCounter.set(10);
        Assert.assertEquals(1, listenerUpdates.get());
    }
    @Test
    public void testTypeSafety() {
        UIStateBus.getOrCreate("typed:num", 42);
        State<String> invalid = UIStateBus.get("typed:num", String.class);
        Assert.assertNull(invalid);
    }
    @Test
    public void testClearDisposesStates() {
        State<Integer> state = UIStateBus.getOrCreate("bus:disposable", 100);
        AtomicInteger notifications = new AtomicInteger(0);
        state.addListener(val -> notifications.incrementAndGet());
        state.set(200);
        Assert.assertEquals(1, notifications.get());
        UIStateBus.clearSessionStates();
        Assert.assertFalse(UIStateBus.has("bus:disposable"));
        state.set(300);
        Assert.assertEquals(1, notifications.get());
    }
}