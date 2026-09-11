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
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
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
        String safeModId = (modId == null || modId.trim().isEmpty()) ? "common" : modId.toLowerCase(java.util.Locale.ROOT);
        String hash = getSHA256(urlStr);
        File modCacheDir = new File(cacheRoot, safeModId);
        if (!modCacheDir.exists()) {
            modCacheDir.mkdirs();
        }
        String ext = ".mp4";
        int lastDot = urlStr.lastIndexOf('.');
        if (lastDot > 0 && urlStr.length() - lastDot <= 5) {
            ext = urlStr.substring(lastDot);
        }
        File cachedFile = new File(modCacheDir, hash + ext);
        if (cachedFile.exists() && cachedFile.length() > 0) {
            cachedFile.setLastModified(System.currentTimeMillis());
            return cachedFile.getAbsolutePath();
        }
        if (activeDownloads.containsKey(hash)) {
            return null;
        }
        activeDownloads.put(hash, urlStr);
        executor.submit(() -> {
            File tmpFile = new File(modCacheDir, hash + ".download.tmp");
            try {
                System.out.println("[X4UI RemoteAssets] Downloading: " + urlStr);
                URL url = new URL(urlStr);
                java.net.URLConnection conn = url.openConnection();
                if (conn instanceof java.net.HttpURLConnection) {
                    java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) conn;
                    httpConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    httpConn.setInstanceFollowRedirects(true);
                }
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(20000);
                if (conn instanceof java.net.HttpURLConnection) {
                    int code = ((java.net.HttpURLConnection) conn).getResponseCode();
                    if (code >= 300 && code < 400) {
                        String newUrl = conn.getHeaderField("Location");
                        if (newUrl != null) {
                            conn = new URL(newUrl).openConnection();
                            if (conn instanceof java.net.HttpURLConnection) {
                                ((java.net.HttpURLConnection) conn).setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                            }
                            conn.setConnectTimeout(10000);
                            conn.setReadTimeout(20000);
                        }
                    }
                    code = ((java.net.HttpURLConnection) conn).getResponseCode();
                    if (code >= 400) {
                        throw new Exception("HTTP Error: " + code);
                    }
                }
                try (java.io.InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(tmpFile)) {
                    byte[] buffer = new byte[16384];
                    int r;
                    while ((r = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, r);
                    }
                }
                if (tmpFile.exists() && tmpFile.length() > 0) {
                    if (cachedFile.exists()) cachedFile.delete();
                    tmpFile.renameTo(cachedFile);
                    System.out.println("[X4UI RemoteAssets] Cached successfully: " + cachedFile.getName());
                }
            } catch (Exception e) {
                System.err.println("[X4UI RemoteAssets] Failed to download: " + urlStr);
                e.printStackTrace();
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                if (cachedFile.exists()) {
                    cachedFile.delete();
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