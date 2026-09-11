package com.x4yi.x4ui.api.client.media;
public interface AssetCallback {
    void onAssetReady(IMediaAsset asset);
    void onError(String errorMessage);
}