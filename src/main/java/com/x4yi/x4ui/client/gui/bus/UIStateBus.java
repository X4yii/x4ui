package com.x4yi.x4ui.client.gui.bus;

import com.x4yi.x4ui.common.State;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class UIStateBus {
    public enum Scope {
        SCREEN,
        SESSION,
        GLOBAL
    }

    private static final Map<String, State<?>> STATES = new ConcurrentHashMap<>();
    private static final Map<String, Scope> SCOPES = new ConcurrentHashMap<>();

    private UIStateBus() {}

    public static <T> State<T> register(String key, State<T> state) {
        return register(key, state, Scope.SESSION);
    }

    public static <T> State<T> register(String key, State<T> state, Scope scope) {
        if (key == null || state == null) {
            throw new IllegalArgumentException("Key and State must not be null");
        }
        STATES.put(key, state);
        SCOPES.put(key, scope != null ? scope : Scope.SESSION);
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
        SCOPES.putIfAbsent(key, scope != null ? scope : Scope.SESSION);
        return (State<T>) STATES.computeIfAbsent(key, k -> new State<>(initialValue));
    }

    @SuppressWarnings("unchecked")
    public static void updateSyncedState(String key, Object value) {
        if (key == null) return;
        State<Object> state = (State<Object>) STATES.get(key);
        if (state == null) {
            SCOPES.putIfAbsent(key, Scope.SESSION);
            state = (State<Object>) STATES.computeIfAbsent(key, k -> new State<>(value));
        }
        state.set(value);
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
            SCOPES.remove(key);
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
        SCOPES.clear();
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
        Iterator<Map.Entry<String, State<?>>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, State<?>> entry = it.next();
            if (entry.getKey().startsWith(prefix)) {
                if (entry.getValue() != null) {
                    entry.getValue().dispose();
                }
                it.remove();
                SCOPES.remove(entry.getKey());
            }
        }
    }

    private static void clearByScope(Scope targetScope) {
        Iterator<Map.Entry<String, State<?>>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, State<?>> entry = it.next();
            String key = entry.getKey();
            Scope scope = SCOPES.getOrDefault(key, Scope.SESSION);
            if (scope == targetScope) {
                if (entry.getValue() != null) {
                    entry.getValue().dispose();
                }
                it.remove();
                SCOPES.remove(key);
            }
        }
    }
}