package com.x4yi.x4ui.api.client.media;

public interface IVideoDecoder extends IMediaDecoder {
    /**
     * @return The width of the video in pixels.
     */
    int getVideoWidth();

    /**
     * @return The height of the video in pixels.
     */
    int getVideoHeight();

    /**
     * Polls the next available frame that should be rendered.
     * @param currentTimeMs The current playback time in milliseconds.
     *                      The decoder will use this to determine if a frame is ready
     *                      or if frame-dropping is necessary to catch up.
     * @return The video frame to render, or null if no frame is ready yet.
     */
    VideoFrame getNextFrame(long currentTimeMs);
    
    /**
     * Optional method to hint the decoder to start decoding into its internal buffers.
     */
    void start();
    
    /**
     * Optional method to pause decoding.
     */
    void pause();
}
