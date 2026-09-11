package com.x4yi.x4ui.api.client.media;
public interface IAudioDecoder extends IMediaDecoder {
    void start();
    void pause();
    long getCurrentAudioTimeMs();
    void setVolume(float volume);
    float getVolume();
    void setMuted(boolean muted);
    boolean isMuted();
}