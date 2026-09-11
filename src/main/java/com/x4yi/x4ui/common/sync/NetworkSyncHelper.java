package com.x4yi.x4ui.common.sync;
import com.x4yi.x4ui.common.State;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
public class NetworkSyncHelper {
    private static final java.util.WeakHashMap<Container, StateContainerListener> CACHE = new java.util.WeakHashMap<>();
    @SideOnly(Side.CLIENT)
    public static void unbindContainer(Container clientContainer) {
        if (clientContainer == null) return;
        StateContainerListener listener = CACHE.remove(clientContainer);
        if (listener != null) {
            clientContainer.removeListener(listener);
            listener.clear();
        }
    }
    @SideOnly(Side.CLIENT)
    public static void bindContainerPropertyToState(Container clientContainer, int propertyId, State<Integer> targetState) {
        StateContainerListener listener = CACHE.get(clientContainer);
        if (listener == null) {
            listener = new StateContainerListener();
            clientContainer.addListener(listener);
            CACHE.put(clientContainer, listener);
        }
        listener.bindProperty(propertyId, targetState);
    }
    @SideOnly(Side.CLIENT)
    public static void bindContainerPropertyToBoolean(Container clientContainer, int propertyId, State<Boolean> targetState) {
        StateContainerListener listener = CACHE.get(clientContainer);
        if (listener == null) {
            listener = new StateContainerListener();
            clientContainer.addListener(listener);
            CACHE.put(clientContainer, listener);
        }
        listener.bindBooleanProperty(propertyId, targetState);
    }
    @SideOnly(Side.CLIENT)
    public static void bindContainerPropertyToFloat(Container clientContainer, int propertyId, State<Float> targetState, float scale) {
        StateContainerListener listener = CACHE.get(clientContainer);
        if (listener == null) {
            listener = new StateContainerListener();
            clientContainer.addListener(listener);
            CACHE.put(clientContainer, listener);
        }
        listener.bindFloatProperty(propertyId, targetState, scale);
    }
    @SideOnly(Side.CLIENT)
    public static void bindContainerPropertyTo32Bit(Container clientContainer, int lowPropId, int highPropId, State<Integer> targetState) {
        StateContainerListener listener = CACHE.get(clientContainer);
        if (listener == null) {
            listener = new StateContainerListener();
            clientContainer.addListener(listener);
            CACHE.put(clientContainer, listener);
        }
        listener.bind32BitProperty(lowPropId, highPropId, targetState);
    }
    @SideOnly(Side.CLIENT)
    public static void bindContainerPropertyToFloat32(Container clientContainer, int lowPropId, int highPropId, State<Float> targetState) {
        StateContainerListener listener = CACHE.get(clientContainer);
        if (listener == null) {
            listener = new StateContainerListener();
            clientContainer.addListener(listener);
            CACHE.put(clientContainer, listener);
        }
        listener.bindFloat32Property(lowPropId, highPropId, targetState);
    }
    public static void send32BitProperty(IContainerListener listener, Container container, int lowPropId, int highPropId, int value) {
        listener.sendWindowProperty(container, lowPropId, value & 0xFFFF);
        listener.sendWindowProperty(container, highPropId, (value >>> 16) & 0xFFFF);
    }
    public static void sendFloat32Property(IContainerListener listener, Container container, int lowPropId, int highPropId, float value) {
        send32BitProperty(listener, container, lowPropId, highPropId, Float.floatToIntBits(value));
    }
}