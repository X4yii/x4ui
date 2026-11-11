package com.x4yi.x4ui.impl.client.media;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
public class MediaPipeline {
    private static final MediaPipeline INSTANCE = new MediaPipeline();
    private final File cacheRoot;
    private final ExecutorService pipelineExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "X4UI-MediaPipeline");
        t.setDaemon(true);
        return t;
    });
    private final ConcurrentHashMap<String, CompletableFuture<File>> inFlight = new ConcurrentHashMap<>();
    private final List<PreloadTask> pendingPreloadTasks = new CopyOnWriteArrayList<>();
    public interface MediaProgressListener {
        void onProgress(String status, int percent);
    }
    public static class PreloadTask {
        public final String modId;
        public final String url;
        public final String fileName;
        public volatile boolean finished = false;
        public volatile boolean failed = false;
        public volatile String status = "Queued";
        public PreloadTask(String modId, String url) {
            this.modId = modId;
            this.url = url;
            int lastSlash = url.lastIndexOf('/');
            this.fileName = (lastSlash >= 0 && lastSlash < url.length() - 1) ? url.substring(lastSlash + 1) : "video.mp4";
        }
    }
    private MediaPipeline() {
        File mcDir = Minecraft.getMinecraft().mcDataDir;
        this.cacheRoot = new File(mcDir, "x4ui/cache/");
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs();
        }
        cleanTemporaryFiles();
    }
    public static MediaPipeline getInstance() {
        return INSTANCE;
    }
    public File getCacheRoot() {
        return cacheRoot;
    }
    public void cleanTemporaryFiles() {
        try {
            File[] modDirs = cacheRoot.listFiles(File::isDirectory);
            if (modDirs != null) {
                for (File dir : modDirs) {
                    File[] tmps = dir.listFiles((d, name) -> name.endsWith(".tmp"));
                    if (tmps != null) {
                        for (File tmp : tmps) {
                            tmp.delete();
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
    public void enqueuePreloads(Map<String, Set<String>> mediaMap) {
        if (mediaMap == null || mediaMap.isEmpty()) return;
        for (Map.Entry<String, Set<String>> entry : mediaMap.entrySet()) {
            String modId = entry.getKey();
            for (String url : entry.getValue()) {
                PreloadTask task = new PreloadTask(modId, url);
                pendingPreloadTasks.add(task);
                obtainMedia(modId, url, (status, percent) -> {
                    task.status = status + (percent >= 0 ? " (" + percent + "%)" : "");
                }).whenComplete((file, err) -> {
                    if (err != null) {
                        task.failed = true;
                        task.status = "Failed";
                    } else {
                        task.finished = true;
                        task.status = "Ready";
                    }
                });
            }
        }
    }
    public List<PreloadTask> getPendingPreloadTasks() {
        return pendingPreloadTasks;
    }
    public CompletableFuture<File> obtainMedia(String modId, String urlStr, MediaProgressListener listener) {
        if (urlStr == null || urlStr.trim().isEmpty()) {
            CompletableFuture<File> failed = new CompletableFuture<>();
            failed.completeExceptionally(new IllegalArgumentException("URL cannot be null or empty"));
            return failed;
        }
        String trimmed = urlStr.trim();
        boolean isHttp = trimmed.startsWith("http://") || trimmed.startsWith("https://");
        if (!isHttp) {
            File localFile = new File(trimmed);
            try {
                File mcDir = (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().mcDataDir != null)
                        ? Minecraft.getMinecraft().mcDataDir.getCanonicalFile()
                        : new File(".").getCanonicalFile();
                File canonicalLocal = localFile.getCanonicalFile();
                if (canonicalLocal.exists() && canonicalLocal.length() > 0
                        && canonicalLocal.getPath().startsWith(mcDir.getPath() + File.separator)) {
                    return CompletableFuture.completedFuture(canonicalLocal);
                }
            } catch (Exception ignored) {
            }
            CompletableFuture<File> failed = new CompletableFuture<>();
            failed.completeExceptionally(new SecurityException("Invalid or disallowed media source: " + trimmed));
            return failed;
        }
        String hash = com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.sha256Hex(trimmed);
        String safeModId = com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.sanitizeModId(modId);
        File modDir;
        File optFile;
        try {
            modDir = com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.resolveSafeChild(cacheRoot, safeModId);
            if (!modDir.exists()) {
                modDir.mkdirs();
            }
            optFile = com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.resolveSafeChild(modDir, hash + "_opt.mp4");
        } catch (Exception e) {
            CompletableFuture<File> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
        if (optFile.exists() && optFile.length() > 1024) {
            if (listener != null) listener.onProgress("Cached", 100);
            return CompletableFuture.completedFuture(optFile);
        }
        CompletableFuture<File> existing = inFlight.get(hash);
        if (existing != null) {
            return existing;
        }
        CompletableFuture<File> future = new CompletableFuture<>();
        inFlight.put(hash, future);
        MediaProgressListener wrappedListener = (status, percent) -> {
            if (listener != null) listener.onProgress(status, percent);
        };
        pipelineExecutor.submit(() -> {
            File rawTmp = new File(modDir, hash + ".download.tmp");
            File transcodeTmp = new File(modDir, hash + ".transcode.tmp");
            try {
                wrappedListener.onProgress("Downloading", 0);
                downloadTo(trimmed, rawTmp, wrappedListener);
                wrappedListener.onProgress("Optimizing", -1);
                optimizeVideo(rawTmp, transcodeTmp);
                if (transcodeTmp.exists() && transcodeTmp.length() > 1024) {
                    if (optFile.exists()) optFile.delete();
                    transcodeTmp.renameTo(optFile);
                    rawTmp.delete();
                } else if (rawTmp.exists() && rawTmp.length() > 1024) {
                    if (optFile.exists()) optFile.delete();
                    rawTmp.renameTo(optFile);
                }
                wrappedListener.onProgress("Ready", 100);
                future.complete(optFile);
            } catch (Throwable e) {
                if (rawTmp.exists()) rawTmp.delete();
                if (transcodeTmp.exists()) transcodeTmp.delete();
                future.completeExceptionally(e);
            } finally {
                inFlight.remove(hash);
            }
        });
        return future;
    }
    private void downloadTo(String urlStr, File destination, MediaProgressListener listener) throws Exception {
        java.net.HttpURLConnection conn = com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.openValidatedHttpConnection(urlStr);
        try {
            long contentLength = conn.getContentLengthLong();
            if (contentLength > com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.MAX_DOWNLOAD_BYTES) {
                throw new java.io.IOException("Remote media exceeds max allowed size (" + contentLength + " bytes)");
            }
            try (InputStream is = conn.getInputStream();
                 FileOutputStream fos = new FileOutputStream(destination)) {
                byte[] buffer = new byte[16384];
                long totalRead = 0;
                int r;
                int lastPercent = -1;
                while ((r = is.read(buffer)) != -1) {
                    totalRead += r;
                    if (totalRead > com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.MAX_DOWNLOAD_BYTES) {
                        throw new java.io.IOException("Download exceeded max allowed size");
                    }
                    fos.write(buffer, 0, r);
                    if (contentLength > 0 && listener != null) {
                        int percent = (int) ((totalRead * 100) / contentLength);
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            listener.onProgress("Downloading", percent);
                        }
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
    }
    private void optimizeVideo(File source, File destination) {
        File ffmpeg = MediaManager.getFfmpegExecutable();
        if (ffmpeg == null || !ffmpeg.exists() || !ffmpeg.canExecute()) {
            try {
                org.apache.commons.io.FileUtils.copyFile(source, destination);
            } catch (Exception ignored) {}
            return;
        }
        try {
            Process probeP = new ProcessBuilder(
                    ffmpeg.getAbsolutePath(),
                    "-i", source.getAbsolutePath()
            ).redirectErrorStream(true).start();
            String probeOutput = IOUtils.toString(probeP.getInputStream(), "UTF-8");
            probeP.waitFor();
            boolean hasAudio = probeOutput.contains("Audio:");
            List<String> cmd = new ArrayList<>();
            cmd.add(ffmpeg.getAbsolutePath());
            cmd.add("-y");
            cmd.add("-loglevel"); cmd.add("quiet");
            cmd.add("-i"); cmd.add(source.getAbsolutePath());
            cmd.add("-vf"); cmd.add("scale='min(1280,iw)':'min(720,ih)':force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2");
            cmd.add("-c:v"); cmd.add("libx264");
            cmd.add("-profile:v"); cmd.add("baseline");
            cmd.add("-level"); cmd.add("3.1");
            cmd.add("-preset"); cmd.add("veryfast");
            cmd.add("-crf"); cmd.add("23");
            cmd.add("-pix_fmt"); cmd.add("yuv420p");
            if (hasAudio) {
                cmd.add("-c:a"); cmd.add("aac");
                cmd.add("-b:a"); cmd.add("128k");
                cmd.add("-ar"); cmd.add("48000");
            } else {
                cmd.add("-an");
            }
            cmd.add("-movflags"); cmd.add("+faststart");
            cmd.add("-threads"); cmd.add("2");
            cmd.add(destination.getAbsolutePath());
            Process transcodeP = new ProcessBuilder(cmd).start();
            transcodeP.waitFor();
        } catch (Exception e) {
            try {
                org.apache.commons.io.FileUtils.copyFile(source, destination);
            } catch (Exception ignored) {}
        }
    }
    private static String getSHA256(String input) {
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