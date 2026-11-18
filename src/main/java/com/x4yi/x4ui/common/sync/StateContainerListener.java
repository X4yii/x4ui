package com.x4yi.x4ui.common.sync;
import com.x4yi.x4ui.common.State;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import java.util.HashMap;
import java.util.Map;
public class StateContainerListener implements IContainerListener {
    private final Map<Integer, java.util.List<java.util.function.IntConsumer>> propertyListeners = new HashMap<>();
    public void bindProperty(int id, State<Integer> state) {
        propertyListeners.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(state::set);
    }
    public void bindBooleanProperty(int id, State<Boolean> state) {
        propertyListeners.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(val -> state.set(val != 0));
    }
    public void bindFloatProperty(int id, State<Float> state, float scale) {
        if (scale == 0.0f || !Float.isFinite(scale)) {
            throw new IllegalArgumentException("Scale must be finite and non-zero");
        }
        propertyListeners.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(val -> state.set(val / scale));
    }
    public void bind32BitProperty(int lowId, int highId, State<Integer> state) {
        if (lowId == highId) {
            throw new IllegalArgumentException("lowId and highId must be distinct");
        }
        Property32Binding binding = new Property32Binding(lowId, highId, state::set);
        propertyListeners.computeIfAbsent(lowId, k -> new java.util.ArrayList<>()).add(binding::updateLow);
        propertyListeners.computeIfAbsent(highId, k -> new java.util.ArrayList<>()).add(binding::updateHigh);
    }
    public void bindFloat32Property(int lowId, int highId, State<Float> state) {
        if (lowId == highId) {
            throw new IllegalArgumentException("lowId and highId must be distinct");
        }
        Property32Binding binding = new Property32Binding(lowId, highId, bits -> state.set(Float.intBitsToFloat(bits)));
        propertyListeners.computeIfAbsent(lowId, k -> new java.util.ArrayList<>()).add(binding::updateLow);
        propertyListeners.computeIfAbsent(highId, k -> new java.util.ArrayList<>()).add(binding::updateHigh);
    }
    private static class Property32Binding {
        private final int lowId;
        private final int highId;
        private final java.util.function.IntConsumer consumer;
        private int currentLow;
        private int currentHigh;
        private boolean hasLow = false;
        private boolean hasHigh = false;
        Property32Binding(int lowId, int highId, java.util.function.IntConsumer consumer) {
            this.lowId = lowId;
            this.highId = highId;
            this.consumer = consumer;
        }
        void updateLow(int val) {
            this.currentLow = val & 0xFFFF;
            this.hasLow = true;
            tryEmit();
        }
        void updateHigh(int val) {
            this.currentHigh = val & 0xFFFF;
            this.hasHigh = true;
            tryEmit();
        }
        private void tryEmit() {
            if (hasLow && hasHigh) {
                consumer.accept((currentHigh << 16) | currentLow);
            }
        }
    }
    @Override
    public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
        java.util.List<java.util.function.IntConsumer> listeners = propertyListeners.get(varToUpdate);
        if (listeners != null) {
            for (java.util.function.IntConsumer listener : listeners) {
                listener.accept(newValue);
            }
        }
    }
    @Override public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {}
    @Override public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {}
    @Override public void sendAllWindowProperties(Container containerIn, IInventory inventory) {}
    public void clear() {
        propertyListeners.clear();
    }
}