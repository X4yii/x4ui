package com.x4yi.x4ui.test;
import com.x4yi.x4ui.common.State;
import com.x4yi.x4ui.common.sync.NetworkSyncHelper;
import com.x4yi.x4ui.common.sync.StateContainerListener;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import org.junit.Assert;
import org.junit.Test;
public class NetworkSync32BitTest {
    @Test
    public void test32BitIntegerSync() {
        StateContainerListener listener = new StateContainerListener();
        State<Integer> rfEnergy = new State<>(0);
        int lowId = 10;
        int highId = 11;
        listener.bind32BitProperty(lowId, highId, rfEnergy);
        int testValue = 1500000;
        int expectedLow = testValue & 0xFFFF;
        int expectedHigh = (testValue >>> 16) & 0xFFFF;
        listener.sendWindowProperty(null, lowId, expectedLow);
        listener.sendWindowProperty(null, highId, expectedHigh);
        Assert.assertEquals(Integer.valueOf(1500000), rfEnergy.get());
        int maxInt = Integer.MAX_VALUE;
        listener.sendWindowProperty(null, lowId, maxInt & 0xFFFF);
        listener.sendWindowProperty(null, highId, (maxInt >>> 16) & 0xFFFF);
        Assert.assertEquals(Integer.valueOf(Integer.MAX_VALUE), rfEnergy.get());
        int negInt = -42000;
        listener.sendWindowProperty(null, lowId, negInt & 0xFFFF);
        listener.sendWindowProperty(null, highId, (negInt >>> 16) & 0xFFFF);
        Assert.assertEquals(Integer.valueOf(-42000), rfEnergy.get());
    }
    @Test
    public void test32BitFloatSync() {
        StateContainerListener listener = new StateContainerListener();
        State<Float> temperature = new State<>(0.0f);
        int lowId = 20;
        int highId = 21;
        listener.bindFloat32Property(lowId, highId, temperature);
        float testFloat = 37.589f;
        int bits = Float.floatToIntBits(testFloat);
        listener.sendWindowProperty(null, lowId, bits & 0xFFFF);
        listener.sendWindowProperty(null, highId, (bits >>> 16) & 0xFFFF);
        Assert.assertEquals(testFloat, temperature.get(), 0.0001f);
    }
}