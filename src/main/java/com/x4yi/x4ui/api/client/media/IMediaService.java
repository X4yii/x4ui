package com.x4yi.x4ui.api.client.media;

import net.minecraft.util.ResourceLocation;

public interface IMediaService {
    
    /**
     * Preloads and processes a local video asset synchronously or asynchronously.
     */
    void preload(ResourceLocation location);
    
    /**
     * Streams or downloads a remote URL asynchronously and returns the asset via callback.
     */
    void streamAsync(String url, AssetCallback callback);
    
    /**
     * Gets a preloaded asset, or null if not loaded.
     */
    IMediaAsset getAsset(String identifier);
}
