package com.x4yi.x4ui.api.client.media;
public interface IVideoDecoder extends IMediaDecoder {
    int getVideoWidth();
    int getVideoHeight();
    VideoFrame getNextFrame(long currentTimeMs);
    void start();
    void pause();
}