package com.x4yi.x4ui.api.client.media;

public class RingBuffer {
    private final VideoFrame[] buffer;
    private int head = 0;
    private int tail = 0;
    private int size = 0;
    private final int capacity;
    
    public RingBuffer(int capacity, int width, int height) {
        this.capacity = capacity;
        this.buffer = new VideoFrame[capacity];
        for (int i = 0; i < capacity; i++) {
            buffer[i] = new VideoFrame(width, height);
        }
    }
    
    public synchronized boolean isFull() {
        return size == capacity;
    }
    
    public synchronized boolean isEmpty() {
        return size == 0;
    }
    
    public synchronized VideoFrame getTail() {
        return buffer[tail];
    }
    
    public synchronized void pushTail() {
        if (size < capacity) {
            tail = (tail + 1) % capacity;
            size++;
        }
    }
    
    public synchronized VideoFrame getHead() {
        return buffer[head];
    }
    
    public synchronized void popHead() {
        if (size > 0) {
            buffer[head].ready = false;
            head = (head + 1) % capacity;
            size--;
        }
    }
    
    public synchronized void clear() {
        head = 0;
        tail = 0;
        size = 0;
        for (VideoFrame f : buffer) {
            f.ready = false;
        }
    }
}
