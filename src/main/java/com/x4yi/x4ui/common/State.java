package com.x4yi.x4ui.common;
import java.util.Objects;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
public class State<T> {
    private T value;
    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    public State(T initialValue) {
        this.value = initialValue;
    }
    public T get() {
        return value;
    }
    public void set(T newValue) {
        if (Objects.equals(this.value, newValue)) {
            return;
        }
        this.value = newValue;
        for (Consumer<T> listener : listeners) {
            listener.accept(newValue);
        }
    }
    public <R> State<R> map(Function<T, R> mapper) {
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