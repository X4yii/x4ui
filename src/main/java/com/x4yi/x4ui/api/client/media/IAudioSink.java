package com.x4yi.x4ui.api.client.media;
public interface IAudioSink {
    void init(int sampleRate, int channels, int bitDepth) throws Exception;
    void writePCM(byte[] data, int offset, int length);
    long getPlaybackTimeMs();
    void start();
    void pause();
    void flush();
    void close();
    void setVolume(float volume);
    float getVolume();
}