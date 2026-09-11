package com.x4yi.x4ui.api.client.media;
public interface IMediaAsset {
    String getIdentifier();
    int getWidth();
    int getHeight();
    double getDuration();
    IVideoDecoder createDecoder();
}