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
        propertyListeners.computeIfAbsent(id, k -> new java.util.ArrayList<>()).add(val -> state.set(val / scale));
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
}