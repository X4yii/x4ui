package com.x4yi.x4ui.api.client.media;
public interface IMediaDecoder {
    void init(String url) throws Exception;
    double getDuration();
    boolean isError();
    String getErrorMessage();
    void close();
    void seek(double timestampSec);
}