package com.x4yi.x4ui.api.client.media;

public interface IMediaAsset {
    String getIdentifier();
    int getWidth();
    int getHeight();
    double getDuration();
    
    /**
     * Creates a new decoder instance for this asset.
     */
    IVideoDecoder createDecoder();
}
