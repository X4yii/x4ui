package com.x4yi.x4ui.common;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
public class State<T> {
    private volatile T value;
    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    public State(T initialValue) {
        this.value = initialValue;
    }
    public T get() {
        return value;
    }
    public void set(T newValue) {
        synchronized (this) {
            if (Objects.equals(this.value, newValue)) {
                return;
            }
            this.value = newValue;
        }
        for (Consumer<T> listener : listeners) {
            if (listener instanceof WeakListener && ((WeakListener<?>) listener).isDead()) {
                listeners.remove(listener);
            } else {
                try {
                    listener.accept(newValue);
                } catch (Exception e) {
                    org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("State listener threw exception", e);
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    public <R> State<R> map(Function<T, R> mapper) {
        State<R> mappedState = new State<>(mapper.apply(this.value));
        WeakListener<T> weakListener = new WeakListener<>(mappedState, (target, val) -> {
            ((State<R>) target).set(mapper.apply(val));
        });
        this.addListener(weakListener);
        return mappedState;
    }
    public Runnable addListenerWithUnbinder(Consumer<T> listener) {
        addListener(listener);
        return () -> removeListener(listener);
    }
    public void addListener(Consumer<T> listener) {
        if (listener == null) return;
        this.listeners.add(listener);
    }
    public void removeListener(Consumer<T> listener) {
        this.listeners.remove(listener);
    }
    public void clearListeners() {
        this.listeners.clear();
    }
    public void dispose() {
        clearListeners();
    }
    public static class WeakListener<T> implements Consumer<T> {
        private final WeakReference<?> targetRef;
        private final BiConsumer<Object, T> handler;
        @SuppressWarnings("unchecked")
        public <Target> WeakListener(Target target, BiConsumer<Target, T> handler) {
            this.targetRef = new WeakReference<>(target);
            this.handler = (BiConsumer<Object, T>) handler;
        }
        @Override
        public void accept(T val) {
            Object target = targetRef.get();
            if (target != null) {
                handler.accept(target, val);
            }
        }
        public boolean isDead() {
            return targetRef.get() == null;
        }
    }
}