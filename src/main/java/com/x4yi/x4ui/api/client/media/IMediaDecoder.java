package com.x4yi.x4ui.api.client.media;

public interface IMediaDecoder {
    /**
     * Initializes the decoder with the given URL or file path.
     * @param url The video/audio URL or absolute file path
     * @throws Exception If initialization fails
     */
    void init(String url) throws Exception;

    /**
     * @return The total duration of the media in seconds. 
     *         May return -1 if unknown or stream is live.
     */
    double getDuration();

    /**
     * @return true if the decoder encountered a fatal error.
     */
    boolean isError();

    /**
     * @return A human-readable error message if isError() is true.
     */
    String getErrorMessage();

    /**
     * Frees all resources associated with this decoder.
     */
    void close();
}
