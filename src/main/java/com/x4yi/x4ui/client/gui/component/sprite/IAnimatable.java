package com.x4yi.x4ui.client.gui.component.sprite;
public interface IAnimatable {
    void play();
    void pause();
    void stop();
    void reset();
    boolean isPlaying();
    void setFPS(float fps);
    float getFPS();
    void setFrame(int frame);
    int getCurrentFrame();
    int getTotalFrames();
    void setLoopMode(LoopMode mode);
    LoopMode getLoopMode();
}