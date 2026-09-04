package com.x4yi.x4ui.api.client.media;

public interface IAudioDecoder extends IMediaDecoder {
    /**
     * Start pushing audio to the sink.
     */
    void start();
    
    /**
     * Pause audio playback.
     */
    void pause();
    
    /**
     * @return The current playback time of the audio hardware in milliseconds.
     *         Used to synchronize the video frame presentation.
     */
    long getCurrentAudioTimeMs();
}
