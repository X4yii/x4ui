package com.x4yi.x4ui.api.client.resource;

import com.x4yi.x4ui.impl.client.resource.network.DownloadManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RemoteResourceManager {
    private static final Map<String, RemoteResourceManager> instances = new HashMap<>();
    
    private final String modId;
    
    private RemoteResourceManager(String modId) {
        this.modId = modId;
    }
    
    /**
     * Creates or gets a RemoteResourceManager instance for a specific mod namespace.
     * This isolates the downloaded caches to .minecraft/x4ui/cache/<modid>/
     * @param modId The ID of your mod
     * @return The isolated resource manager
     */
    public static RemoteResourceManager create(String modId) {
        if (!instances.containsKey(modId)) {
            instances.put(modId, new RemoteResourceManager(modId));
        }
        return instances.get(modId);
    }
    
    /**
     * Requests a remote video or audio file.
     * Returns an absolute local path immediately if cached, or a temporary fallback path,
     * while the download happens in the background.
     */
    public String getMedia(String url) {
        return DownloadManager.getInstance().requestMedia(modId, url);
    }
    
    /**
     * Cleans up files in this mod's cache namespace that haven't been modified in X days.
     * @param days Max age in days
     */
    public void cleanOldCache(int days) {
        DownloadManager.getInstance().cleanCache(modId, days);
    }
}
