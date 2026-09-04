package com.x4yi.x4ui.api.client.media;

public class VideoFrame {
    public int[] pixels;
    public double pts; // Presentation Timestamp in seconds
    public volatile boolean ready = false;
    
    public VideoFrame(int width, int height) {
        pixels = new int[width * height];
    }
}
