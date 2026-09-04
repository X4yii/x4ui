package com.x4yi.x4ui.impl.client.media;

import com.x4yi.x4ui.api.client.media.AssetCallback;
import com.x4yi.x4ui.api.client.media.IMediaAsset;
import com.x4yi.x4ui.api.client.media.IMediaService;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaService implements IMediaService {

    private final ConcurrentHashMap<String, IMediaAsset> assetCache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @Override
    public void preload(ResourceLocation location) {
        // For local assets, we can usually just create the asset directly.
        // In a full implementation, we'd extract from JAR to a temp file, then map.
        // For now, we simulate success for local ResourceLocations.
        String id = location.toString();
        if (!assetCache.containsKey(id)) {
            // Local assets will be handled transparently by the decoder if passed the correct path.
            // But we need a real path. Minecraft's ResourcePack system makes this complex.
            // Phase 2 feature: Extract ResourceLocation to java.io.File cache.
        }
    }

    @Override
    public void streamAsync(String url, AssetCallback callback) {
        if (assetCache.containsKey(url)) {
            callback.onAssetReady(assetCache.get(url));
            return;
        }
        
        executor.submit(() -> {
            try {
                // Here we would download the MP4 file to a cache folder, 
                // and then return a MappedMediaAsset pointing to the local MP4.
                // Since the user is using `yt-dlp` or similar, we assume the URL is a direct video 
                // or we pass it to FFmpeg natively.
                
                // For this implementation, we just wrap it into an IMediaAsset 
                // that will spawn a DirectMemoryVideoDecoder when createDecoder() is called.
                
                IMediaAsset asset = new IMediaAsset() {
                    @Override
                    public String getIdentifier() {
                        return url;
                    }

                    @Override
                    public int getWidth() { return 1280; } // Resolved during decoder init

                    @Override
                    public int getHeight() { return 720; }

                    @Override
                    public double getDuration() { return -1; }

                    @Override
                    public IVideoDecoder createDecoder() {
                        return new DirectMemoryVideoDecoder();
                    }
                };
                
                assetCache.put(url, asset);
                callback.onAssetReady(asset);
                
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    @Override
    public IMediaAsset getAsset(String identifier) {
        return assetCache.get(identifier);
    }
}
