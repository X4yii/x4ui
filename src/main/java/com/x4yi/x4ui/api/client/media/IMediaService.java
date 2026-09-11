package com.x4yi.x4ui.api.client.media;
import net.minecraft.util.ResourceLocation;
public interface IMediaService {
    void preload(ResourceLocation location);
    void streamAsync(String url, AssetCallback callback);
    IMediaAsset getAsset(String identifier);
}