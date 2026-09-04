package com.x4yi.x4ui.api.client.media;

public interface IAudioSink {
    /**
     * Initializes the audio sink with the given format.
     * @param sampleRate e.g. 44100, 48000
     * @param channels e.g. 1 for mono, 2 for stereo
     * @param bitDepth e.g. 8 or 16
     * @throws Exception if OpenAL initialization fails
     */
    void init(int sampleRate, int channels, int bitDepth) throws Exception;

    /**
     * Queues PCM data for playback.
     * @param data Raw PCM bytes
     * @param offset Offset in the array
     * @param length Length of data to write
     */
    void writePCM(byte[] data, int offset, int length);

    /**
     * @return The current playback time in milliseconds based on hardware sample offset.
     */
    long getPlaybackTimeMs();

    void start();
    
    void pause();
    
    void flush();
    
    void close();
}
