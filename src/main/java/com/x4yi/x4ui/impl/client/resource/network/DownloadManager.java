package com.x4yi.x4ui.impl.client.resource.network;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SideOnly(Side.CLIENT)
public class DownloadManager {
    private static final DownloadManager INSTANCE = new DownloadManager();
    
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // 2 concurrent downloads max
    private final ConcurrentHashMap<String, String> activeDownloads = new ConcurrentHashMap<>();
    
    private File cacheRoot;
    
    private DownloadManager() {
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        cacheRoot = new File(mcDir, "x4ui/cache/");
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs();
        }
    }
    
    public static DownloadManager getInstance() {
        return INSTANCE;
    }
    
    public String requestMedia(String modId, String urlStr) {
        String hash = getSHA256(urlStr);
        File modCacheDir = new File(cacheRoot, modId);
        if (!modCacheDir.exists()) {
            modCacheDir.mkdirs();
        }
        
        // Extract extension from URL, defaulting to .mp4
        String ext = ".mp4";
        int lastDot = urlStr.lastIndexOf('.');
        if (lastDot > 0 && urlStr.length() - lastDot <= 5) {
            ext = urlStr.substring(lastDot);
        }
        
        File cachedFile = new File(modCacheDir, hash + ext);
        if (cachedFile.exists() && cachedFile.length() > 0) {
            // Touch the file to update its modified time for auto-cleaner
            cachedFile.setLastModified(System.currentTimeMillis());
            return cachedFile.getAbsolutePath();
        }
        
        // Prevent multiple downloads of the same URL
        if (activeDownloads.containsKey(hash)) {
            return null; // Return null so the GUI component waits or shows a loading state
        }
        
        activeDownloads.put(hash, urlStr);
        
        executor.submit(() -> {
            try {
                System.out.println("[X4UI RemoteAssets] Downloading: " + urlStr);
                URL url = new URL(urlStr);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(cachedFile);
                // NIO Zero-Copy transfer
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                rbc.close();
                System.out.println("[X4UI RemoteAssets] Cached successfully: " + cachedFile.getName());
            } catch (Exception e) {
                System.err.println("[X4UI RemoteAssets] Failed to download: " + urlStr);
                e.printStackTrace();
                if (cachedFile.exists()) {
                    cachedFile.delete(); // cleanup corrupted partial download
                }
            } finally {
                activeDownloads.remove(hash);
            }
        });
        
        return null;
    }
    
    public void cleanCache(String modId, int daysLimit) {
        File modCacheDir = new File(cacheRoot, modId);
        if (!modCacheDir.exists()) return;
        
        long limitMs = System.currentTimeMillis() - (daysLimit * 24L * 60L * 60L * 1000L);
        File[] files = modCacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < limitMs) {
                    file.delete();
                }
            }
        }
    }
    
    private String getSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
