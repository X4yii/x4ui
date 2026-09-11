package com.x4yi.x4ui.api.client.resource;
import com.x4yi.x4ui.impl.client.resource.network.DownloadManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
@SideOnly(Side.CLIENT)
public class RemoteResourceManager {
    private static final Map<String, RemoteResourceManager> instances = new java.util.concurrent.ConcurrentHashMap<>();
    private final String modId;
    private RemoteResourceManager(String modId) {
        this.modId = modId;
    }
    public static RemoteResourceManager create(String modId) {
        return instances.computeIfAbsent(modId, RemoteResourceManager::new);
    }
    public String getMedia(String url) {
        return DownloadManager.getInstance().requestMedia(modId, url);
    }
    public void cleanOldCache(int days) {
        DownloadManager.getInstance().cleanCache(modId, days);
    }
}