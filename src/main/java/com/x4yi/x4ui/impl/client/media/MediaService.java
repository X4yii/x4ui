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
        String id = location.toString();
        if (!assetCache.containsKey(id)) {
        }
    }
    @Override
    public void streamAsync(String url, AssetCallback callback) {
        if (assetCache.containsKey(url)) {
            callback.onAssetReady(assetCache.get(url));
            return;
        }
        MediaPipeline.getInstance().obtainMedia("global", url, null).whenComplete((file, err) -> {
            if (err != null) {
                callback.onError(err.getMessage());
                return;
            }
            try {
                String localPath = file.getAbsolutePath();
                IMediaAsset asset = new IMediaAsset() {
                    @Override
                    public String getIdentifier() {
                        return localPath;
                    }
                    @Override
                    public int getWidth() { return 1280; }
                    @Override
                    public int getHeight() { return 720; }
                    @Override
                    public double getDuration() { return -1; }
                    @Override
                    public IVideoDecoder createDecoder() {
                        if (MediaManager.isFFmpegAvailable()) {
                            return new DirectMemoryVideoDecoder();
                        } else {
                            return new com.x4yi.x4ui.impl.client.media.backend.jcodec.JCodecVideoDecoder();
                        }
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