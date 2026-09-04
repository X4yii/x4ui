package com.x4yi.x4ui.common.sync;
import com.x4yi.x4ui.common.State;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
public class NetworkSyncHelper {
    private static final java.util.WeakHashMap<Container, StateContainerListener> CACHE = new java.util.WeakHashMap<>();
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
}