package com.x4yi.x4ui.client.gui.bus;

import com.x4yi.x4ui.common.State;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SideOnly(Side.CLIENT)
public final class UIStateBus {
    public enum Scope {
        SCREEN,
        SESSION,
        GLOBAL
    }

    private static final class BusEntry {
        final State<?> state;
        final Scope scope;
        @Nullable final Class<?> valueType;

        BusEntry(State<?> state, Scope scope, @Nullable Class<?> valueType) {
            this.state = state;
            this.scope = scope;
            this.valueType = valueType;
        }
    }

    private static final Map<String, BusEntry> ENTRIES = new ConcurrentHashMap<>();

    private UIStateBus() {}

    public static <T> State<T> register(String key, State<T> state) {
        return register(key, state, Scope.SESSION);
    }

    public static <T> State<T> register(String key, State<T> state, Scope scope) {
        if (key == null || state == null) {
            throw new IllegalArgumentException("Key and State must not be null");
        }
        T current = state.get();
        Class<?> type = current != null ? current.getClass() : null;
        BusEntry previous = ENTRIES.put(key, new BusEntry(state, scope != null ? scope : Scope.SESSION, type));
        if (previous != null && previous.state != null && previous.state != state) {
            previous.state.dispose();
        }
        return state;
    }

    public static <T> State<T> getOrCreate(String key, T initialValue) {
        return getOrCreate(key, initialValue, Scope.SESSION);
    }

    @SuppressWarnings("unchecked")
    public static <T> State<T> getOrCreate(String key, T initialValue, Scope scope) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Scope resolvedScope = scope != null ? scope : Scope.SESSION;
        Class<?> type = initialValue != null ? initialValue.getClass() : null;
        BusEntry entry = ENTRIES.computeIfAbsent(key, k -> new BusEntry(new State<>(initialValue), resolvedScope, type));
        return (State<T>) entry.state;
    }

    @SuppressWarnings("unchecked")
    public static void updateSyncedState(String key, Object value) {
        if (key == null) return;
        Class<?> type = value != null ? value.getClass() : null;
        BusEntry entry = ENTRIES.computeIfAbsent(key, k -> new BusEntry(new State<>(value), Scope.SESSION, type));
        if (value != null && entry.valueType != null && !entry.valueType.isInstance(value)) {
            return;
        }
        ((State<Object>) entry.state).set(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> State<T> get(String key, Class<T> expectedType) {
        if (key == null || expectedType == null) return null;
        BusEntry entry = ENTRIES.get(key);
        if (entry != null && entry.state != null) {
            Object val = entry.state.get();
            if (val != null) {
                return expectedType.isInstance(val) ? (State<T>) entry.state : null;
            }
            if (entry.valueType != null) {
                return expectedType.isAssignableFrom(entry.valueType) ? (State<T>) entry.state : null;
            }
            return (State<T>) entry.state;
        }
        return null;
    }

    public static boolean has(String key) {
        return key != null && ENTRIES.containsKey(key);
    }

    public static void remove(String key) {
        if (key != null) {
            BusEntry removed = ENTRIES.remove(key);
            if (removed != null && removed.state != null) {
                removed.state.dispose();
            }
        }
    }

    public static Set<String> getRegisteredKeys() {
        return Collections.unmodifiableSet(ENTRIES.keySet());
    }

    public static void clear() {
        for (BusEntry entry : ENTRIES.values()) {
            if (entry != null && entry.state != null) {
                entry.state.dispose();
            }
        }
        ENTRIES.clear();
    }

    public static void clearSessionStates() {
        clearByScope(Scope.SESSION);
    }

    public static void clearScreenStates() {
        clearByScope(Scope.SCREEN);
    }

    public static String scopedKey(String namespace, String key) {
        String ns = namespace != null ? namespace : "";
        String k = key != null ? key : "";
        return ns.isEmpty() ? k : (ns + ":" + k);
    }

    public static void clearNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) return;
        String prefix = namespace + ":";
        Iterator<Map.Entry<String, BusEntry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BusEntry> entry = it.next();
            if (entry.getKey().startsWith(prefix)) {
                BusEntry val = entry.getValue();
                if (val != null && val.state != null) {
                    val.state.dispose();
                }
                it.remove();
            }
        }
    }

    private static void clearByScope(Scope targetScope) {
        Iterator<Map.Entry<String, BusEntry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, BusEntry> entry = it.next();
            BusEntry val = entry.getValue();
            if (val != null && val.scope == targetScope) {
                if (val.state != null) {
                    val.state.dispose();
                }
                it.remove();
            }
        }
    }
}
