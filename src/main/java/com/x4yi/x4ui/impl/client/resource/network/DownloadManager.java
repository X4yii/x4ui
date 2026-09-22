package com.x4yi.x4ui.impl.client.resource.network;

import com.x4yi.x4ui.common.platform.PlatformBridge;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@SideOnly(Side.CLIENT)
public class DownloadManager {
    private static final DownloadManager INSTANCE = new DownloadManager();
    private final ExecutorService executor = PlatformBridge.newIoExecutor("X4UI-Download", 2);
    private final ConcurrentHashMap<String, String> activeDownloads = new ConcurrentHashMap<>();
    private final File cacheRoot;

    private DownloadManager() {
        Minecraft mc = Minecraft.getMinecraft();
        File mcDir = (mc != null && mc.mcDataDir != null) ? mc.mcDataDir : new File(".");
        cacheRoot = new File(mcDir, "x4ui/cache/");
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs();
        }
    }

    public static DownloadManager getInstance() {
        return INSTANCE;
    }

    public String requestMedia(String modId, String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty()) {
            return null;
        }
        final String trimmedUrl = urlStr.trim();
        final String safeModId = SecurityUtils.sanitizeModId(modId);
        final String hash = SecurityUtils.sha256Hex(trimmedUrl);
        final String ext = SecurityUtils.sanitizeExtension(trimmedUrl, ".mp4");

        final File modCacheDir;
        final File cachedFile;
        try {
            modCacheDir = SecurityUtils.resolveSafeChild(cacheRoot, safeModId);
            if (!modCacheDir.exists()) {
                modCacheDir.mkdirs();
            }
            cachedFile = SecurityUtils.resolveSafeChild(modCacheDir, hash + ext);
        } catch (IOException | SecurityException e) {
            org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("[X4UI RemoteAssets] Blocked unsafe cache path: {}", e.getMessage());
            return null;
        }

        if (cachedFile.exists() && cachedFile.length() > 0) {
            cachedFile.setLastModified(System.currentTimeMillis());
            return cachedFile.getAbsolutePath();
        }

        if (activeDownloads.putIfAbsent(hash, trimmedUrl) != null) {
            return null;
        }

        executor.submit(() -> {
            File tmpFile = new File(modCacheDir, hash + ".download.tmp");
            HttpURLConnection conn = null;
            try {
                org.apache.logging.log4j.LogManager.getLogger("x4ui").info("[X4UI RemoteAssets] Downloading: {}", trimmedUrl);
                conn = SecurityUtils.openValidatedHttpConnection(trimmedUrl);
                long contentLength = conn.getContentLengthLong();
                if (contentLength > SecurityUtils.MAX_DOWNLOAD_BYTES) {
                    throw new IOException("Remote file exceeds maximum allowed size (" + contentLength + " bytes)");
                }
                try (InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(tmpFile)) {
                    byte[] buffer = new byte[16384];
                    long totalRead = 0;
                    int r;
                    while ((r = is.read(buffer)) != -1) {
                        totalRead += r;
                        if (totalRead > SecurityUtils.MAX_DOWNLOAD_BYTES) {
                            throw new IOException("Download exceeded maximum allowed size (" + SecurityUtils.MAX_DOWNLOAD_BYTES + " bytes)");
                        }
                        fos.write(buffer, 0, r);
                    }
                }
                if (tmpFile.exists() && tmpFile.length() > 0) {
                    if (cachedFile.exists()) cachedFile.delete();
                    if (!tmpFile.renameTo(cachedFile)) {
                        throw new IOException("Failed to move downloaded file to cache");
                    }
                    org.apache.logging.log4j.LogManager.getLogger("x4ui").info("[X4UI RemoteAssets] Cached successfully: {}", cachedFile.getName());
                }
            } catch (Exception e) {
                org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("[X4UI RemoteAssets] Failed to download: {} ({})", trimmedUrl, e.getMessage());
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                if (cachedFile.exists()) {
                    cachedFile.delete();
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                activeDownloads.remove(hash);
            }
        });
        return null;
    }

    public void cleanCache(String modId, int daysLimit) {
        String safeModId = SecurityUtils.sanitizeModId(modId);
        try {
            File modCacheDir = SecurityUtils.resolveSafeChild(cacheRoot, safeModId);
            if (!modCacheDir.exists() || !modCacheDir.isDirectory()) return;
            long limitMs = System.currentTimeMillis() - (daysLimit * 24L * 60L * 60L * 1000L);
            File[] files = modCacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.lastModified() < limitMs) {
                        file.delete();
                    }
                }
            }
        } catch (IOException | SecurityException e) {
            org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("[X4UI RemoteAssets] Blocked unsafe cache clean: {}", e.getMessage());
        }
    }
}
