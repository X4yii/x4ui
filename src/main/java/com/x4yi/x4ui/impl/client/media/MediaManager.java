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
    // For production, we'd use a raw direct link to an actual binary for each OS to avoid zip extraction here,
    // or we use a dedicated X4UI-FFmpeg mirror.
    // For this Phase 3 scaffolding, we'll simulate the download for simplicity and define the framework.

    private static boolean mediaEngineRequired = false;
    private static AtomicBoolean downloadFinished = new AtomicBoolean(false);
    private static AtomicInteger downloadProgress = new AtomicInteger(0);
    private static String downloadStatus = "";
    
    private static File binDir;
    private static File ffmpegExecutable;
    
    public static void requireMediaEngine() {
        if (mediaEngineRequired) return;
        mediaEngineRequired = true;
        
        // Start background download thread
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
        }
    }
    
    private static void downloadFFmpegProcess() {
        try {
            downloadStatus = "Checking FFmpeg Cache...";
            
            if (ffmpegExecutable.exists()) {
                downloadStatus = "Verifying Integrity...";
                String hash = getSHA256(ffmpegExecutable);
                // Here we would compare 'hash' with the expected hash for this OS/Arch
                // If match:
                downloadFinished.set(true);
                System.out.println("[X4UI Media] FFmpeg is ready! Hash: " + hash);
                return;
            }
            
            downloadStatus = "Downloading FFmpeg...";
            downloadProgress.set(0);
            
            // SIMULATED DOWNLOAD FOR NOW
            // In a real scenario, this would open a stream to the direct binary URL 
            // and write to ffmpegExecutable, updating downloadProgress from 0 to 100.
            for (int i = 0; i <= 100; i += 10) {
                Thread.sleep(100); 
                downloadProgress.set(i);
            }
            
            // Dummy file creation for now
            ffmpegExecutable.createNewFile();
            ffmpegExecutable.setExecutable(true);
            
            downloadFinished.set(true);
            downloadStatus = "FFmpeg Downloaded!";
            System.out.println("[X4UI Media] FFmpeg Download Complete!");
            
        } catch (Exception e) {
            System.err.println("[X4UI Media] FFmpeg Download Failed: " + e.getMessage());
            e.printStackTrace();
            // Even if it fails, we set finished to true so we don't block the splash screen forever.
            // The system will just fallback to JCodec at runtime.
            downloadFinished.set(true); 
        }
    }
    
    public static void blockUntilDownloaded() {
        if (!mediaEngineRequired) return;
        if (downloadFinished.get()) return;
        
        ProgressManager.ProgressBar bar = ProgressManager.push("X4UI Media Engine", 100);
        int lastProgress = 0;
        
        while (!downloadFinished.get()) {
            int current = downloadProgress.get();
            if (current > lastProgress) {
                for (int i = lastProgress; i < current; i++) {
                    bar.step(downloadStatus);
                }
                lastProgress = current;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
        
        // Catch up the bar to 100 if it finished
        for (int i = lastProgress; i < 100; i++) {
            bar.step("Done");
        }
        
        ProgressManager.pop(bar);
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
