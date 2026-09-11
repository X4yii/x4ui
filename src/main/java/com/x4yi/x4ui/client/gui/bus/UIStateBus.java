package com.x4yi.x4ui.client.gui.bus;
import com.x4yi.x4ui.common.State;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public final class UIStateBus {
    private static final Map<String, State<?>> STATES = new ConcurrentHashMap<>();
    private UIStateBus() {}
    public static <T> State<T> register(String key, State<T> state) {
        if (key == null || state == null) {
            throw new IllegalArgumentException("Key and State must not be null");
        }
        STATES.put(key, state);
        return state;
    }
    @SuppressWarnings("unchecked")
    public static <T> State<T> getOrCreate(String key, T initialValue) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        return (State<T>) STATES.computeIfAbsent(key, k -> new State<>(initialValue));
    }
    @SuppressWarnings("unchecked")
    public static <T> State<T> get(String key, Class<T> expectedType) {
        if (key == null) return null;
        State<?> state = STATES.get(key);
        if (state != null) {
            Object val = state.get();
            if (val == null || expectedType.isInstance(val)) {
                return (State<T>) state;
            }
        }
        return null;
    }
    public static boolean has(String key) {
        return key != null && STATES.containsKey(key);
    }
    public static void remove(String key) {
        if (key != null) {
            State<?> removed = STATES.remove(key);
            if (removed != null) {
                removed.dispose();
            }
        }
    }
    public static Set<String> getRegisteredKeys() {
        return Collections.unmodifiableSet(STATES.keySet());
    }
    public static void clear() {
        for (State<?> state : STATES.values()) {
            if (state != null) {
                state.dispose();
            }
        }
        STATES.clear();
    }
    public static void clearSessionStates() {
        clear();
    }
}