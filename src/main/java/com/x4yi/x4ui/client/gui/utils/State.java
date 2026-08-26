package com.x4yi.x4ui.client.gui.utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
public class State<T> {
    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    public State(T initialValue) {
        this.value = initialValue;
    }
    public T get() {
        return value;
    }
    public void set(T newValue) {
        this.value = newValue;
        for (Consumer<T> listener : listeners) {
            listener.accept(newValue);
        }
    }
    public <R> State<R> map(java.util.function.Function<T, R> mapper) {
        State<R> mappedState = new State<>(mapper.apply(this.value));
        this.addListener(val -> mappedState.set(mapper.apply(val)));
        return mappedState;
    }
    public void addListener(Consumer<T> listener) {
        this.listeners.add(listener);
    }
    public void removeListener(Consumer<T> listener) {
        this.listeners.remove(listener);
    }
    public void clearListeners() {
        this.listeners.clear();
    }
}