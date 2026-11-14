package com.x4yi.x4ui.impl.client.media;
import net.minecraftforge.fml.common.ProgressManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
public class MediaManager {
    private static final Logger LOGGER = LogManager.getLogger("x4ui");
    private static boolean mediaEngineRequired = false;
    private static AtomicBoolean downloadFinished = new AtomicBoolean(false);
    private static AtomicInteger downloadProgress = new AtomicInteger(0);
    private static String downloadStatus = "";
    private static File binDir;
    private static volatile File ffmpegExecutable;
    private static volatile String ffmpegSource = "not-found";
    public static void requireMediaEngine() {
        if (mediaEngineRequired) return;
        mediaEngineRequired = true;
        Thread downloader = new Thread(MediaManager::downloadFFmpegProcess);
        downloader.setName("X4UI-FFmpeg-Resolver");
        downloader.setDaemon(true);
        downloader.start();
    }
    public static void setBinDir(File minecraftDir) {
        if (minecraftDir != null) {
            binDir = new File(minecraftDir, "x4ui/bin/");
        }
        resolveFFmpegExecutable();
    }

    public static synchronized File resolveFFmpegExecutable() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        boolean isWindows = os.contains("win");
        String exeName = isWindows ? "ffmpeg.exe" : "ffmpeg";

        String propOverride = System.getProperty("x4ui.ffmpeg.path");
        if (propOverride != null && !propOverride.trim().isEmpty()) {
            File f = new File(propOverride.trim());
            if (isValidExecutable(f)) {
                ffmpegExecutable = f;
                ffmpegSource = "system-property";
                return f;
            }
        }
        String envOverride = System.getenv("X4UI_FFMPEG");
        if (envOverride != null && !envOverride.trim().isEmpty()) {
            File f = new File(envOverride.trim());
            if (isValidExecutable(f)) {
                ffmpegExecutable = f;
                ffmpegSource = "env-var";
                return f;
            }
        }

        String pathEnv = System.getenv("PATH");
        if (pathEnv != null && !pathEnv.isEmpty()) {
            String[] dirs = pathEnv.split(File.pathSeparator);
            for (String dir : dirs) {
                if (dir == null || dir.trim().isEmpty()) continue;
                File candidate = new File(dir.trim(), exeName);
                if (isValidExecutable(candidate)) {
                    ffmpegExecutable = candidate;
                    ffmpegSource = "system-path";
                    return candidate;
                }
            }
        }

        for (File candidate : getOsCandidatePaths(os, exeName)) {
            if (isValidExecutable(candidate)) {
                ffmpegExecutable = candidate;
                ffmpegSource = "os-location";
                return candidate;
            }
        }

        if (binDir != null) {
            File fallback = new File(binDir, exeName);
            if (isValidExecutable(fallback)) {
                ffmpegExecutable = fallback;
                ffmpegSource = "instance-bin";
                return fallback;
            }
        }

        ffmpegExecutable = null;
        ffmpegSource = "not-found";
        return null;
    }

    private static List<File> getOsCandidatePaths(String osLower, String exeName) {
        List<File> candidates = new ArrayList<>();
        String userHome = System.getProperty("user.home", "");
        if (osLower.contains("win")) {
            String localAppData = System.getenv("LOCALAPPDATA");
            String programFiles = System.getenv("ProgramFiles");
            String programFilesX86 = System.getenv("ProgramFiles(x86)");
            String programData = System.getenv("ProgramData");
            if (localAppData != null) {
                candidates.add(new File(localAppData, "Microsoft/WinGet/Links/" + exeName));
                candidates.add(new File(localAppData, "Programs/ffmpeg/bin/" + exeName));
            }
            if (!userHome.isEmpty()) {
                candidates.add(new File(userHome, "scoop/shims/" + exeName));
            }
            if (programData != null) {
                candidates.add(new File(programData, "chocolatey/bin/" + exeName));
            }
            candidates.add(new File("C:/ffmpeg/bin/" + exeName));
            if (programFiles != null) {
                candidates.add(new File(programFiles, "ffmpeg/bin/" + exeName));
            }
            if (programFilesX86 != null) {
                candidates.add(new File(programFilesX86, "ffmpeg/bin/" + exeName));
            }
        } else if (osLower.contains("mac") || osLower.contains("darwin")) {
            candidates.add(new File("/opt/homebrew/bin/" + exeName));
            candidates.add(new File("/usr/local/bin/" + exeName));
            candidates.add(new File("/opt/local/bin/" + exeName));
            if (!userHome.isEmpty()) {
                candidates.add(new File(userHome, ".local/bin/" + exeName));
            }
        } else {
            candidates.add(new File("/usr/bin/" + exeName));
            candidates.add(new File("/usr/local/bin/" + exeName));
            candidates.add(new File("/snap/bin/" + exeName));
            candidates.add(new File("/var/lib/flatpak/exports/bin/" + exeName));
            candidates.add(new File("/home/linuxbrew/.linuxbrew/bin/" + exeName));
            if (!userHome.isEmpty()) {
                candidates.add(new File(userHome, ".local/bin/" + exeName));
            }
        }
        return candidates;
    }

    private static boolean isValidExecutable(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        if (!file.canExecute()) {
            file.setExecutable(true);
        }
        return file.length() > 1024 && file.canExecute();
    }

    public static boolean isFFmpegAvailable() {
        if (ffmpegExecutable == null) {
            resolveFFmpegExecutable();
        }
        return isValidExecutable(ffmpegExecutable);
    }

    public static String getFFmpegSource() {
        return ffmpegSource;
    }

    private static void downloadFFmpegProcess() {
        try {
            downloadStatus = "Locating OS FFmpeg...";
            downloadProgress.set(30);
            File resolved = resolveFFmpegExecutable();
            if (resolved != null) {
                downloadStatus = "FFmpeg found (" + ffmpegSource + ")";
                String hash = getSHA256(resolved);
                downloadProgress.set(100);
                LOGGER.info("[X4UI Media] FFmpeg ready ({}): {} [sha256={}]", ffmpegSource, resolved.getAbsolutePath(), hash);
            } else {
                downloadStatus = "FFmpeg not found on OS (JCodec fallback active)";
                downloadProgress.set(100);
                LOGGER.warn("[X4UI Media] FFmpeg executable not found in PATH or standard OS directories; video playback will fall back to JCodec.");
            }
        } catch (Exception e) {
            LOGGER.warn("[X4UI Media] FFmpeg resolution failed: {}", e.getMessage());
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