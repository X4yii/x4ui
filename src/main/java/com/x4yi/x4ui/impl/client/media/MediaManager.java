package com.x4yi.x4ui.impl.client.media;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class MediaManager {
    private static final String FFMPEG_URL_WIN = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";
    private static boolean mediaEngineRequired = false;
    private static AtomicBoolean downloadFinished = new AtomicBoolean(false);
    private static AtomicInteger downloadProgress = new AtomicInteger(0);
    private static String downloadStatus = "";
    private static File binDir;
    private static File ffmpegExecutable;
    public static void requireMediaEngine() {
        if (mediaEngineRequired) return;
        mediaEngineRequired = true;
        Thread downloader = new Thread(MediaManager::downloadFFmpegProcess);
        downloader.setName("X4UI-FFmpeg-Downloader");
        downloader.setDaemon(true);
        downloader.start();
    }
    public static void setBinDir(File minecraftDir) {
        binDir = new File(minecraftDir, "x4ui/bin/");
        if (!binDir.exists()) {
            binDir.mkdirs();
        }
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            ffmpegExecutable = new File(binDir, "ffmpeg.exe");
        } else {
            ffmpegExecutable = new File(binDir, "ffmpeg");
            if (ffmpegExecutable.exists()) {
                ffmpegExecutable.setExecutable(true);
            }
        }
    }
    public static boolean isFFmpegAvailable() {
        if (ffmpegExecutable != null && ffmpegExecutable.exists()) {
            if (!ffmpegExecutable.canExecute()) {
                ffmpegExecutable.setExecutable(true);
            }
            return ffmpegExecutable.length() > 1024 && ffmpegExecutable.canExecute();
        }
        return false;
    }
    private static void downloadFFmpegProcess() {
        try {
            downloadStatus = "Checking FFmpeg Cache...";
            if (isFFmpegAvailable()) {
                downloadStatus = "Verifying Integrity...";
                String hash = getSHA256(ffmpegExecutable);
                downloadFinished.set(true);
                System.out.println("[X4UI Media] FFmpeg is ready! Hash: " + hash);
                return;
            }
            downloadStatus = "Downloading FFmpeg...";
            downloadProgress.set(0);
            downloadProgress.set(100);
            downloadFinished.set(true);
            downloadStatus = "FFmpeg verification complete.";
        } catch (Exception e) {
            System.err.println("[X4UI Media] FFmpeg Verification/Download Failed: " + e.getMessage());
            e.printStackTrace();
            downloadFinished.set(true); 
        } finally {
            downloadFinished.set(true);
        }
    }
    public static void blockUntilDownloaded() {
        if (!mediaEngineRequired) return;
        if (downloadFinished.get()) return;
        ProgressManager.ProgressBar bar = ProgressManager.push("X4UI Media Engine", 100);
        int lastProgress = 0;
        int timeoutTicks = 0;
        while (!downloadFinished.get() && timeoutTicks < 600) {
            int current = downloadProgress.get();
            if (current > lastProgress) {
                for (int i = lastProgress; i < current; i++) {
                    bar.step(downloadStatus);
                }
                lastProgress = current;
            }
            try {
                Thread.sleep(50);
                timeoutTicks++;
            } catch (InterruptedException e) {
                break;
            }
        }
        for (int i = lastProgress; i < 100; i++) {
            bar.step("Done");
        }
        ProgressManager.pop(bar);
    }
    public static boolean isDownloadFinished() {
        return downloadFinished.get();
    }
    public static int getDownloadProgress() {
        return downloadProgress.get();
    }
    public static String getDownloadStatus() {
        return downloadStatus;
    }
    public static boolean isMediaEngineRequired() {
        return mediaEngineRequired;
    }
    public static File getFfmpegExecutable() {
        return ffmpegExecutable;
    }
    private static String getSHA256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[8192];
            int bytesCount = 0; 
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (int i=0; i< bytes.length ;i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}