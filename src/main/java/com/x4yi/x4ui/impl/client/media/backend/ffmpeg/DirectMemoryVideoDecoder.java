package com.x4yi.x4ui.impl.client.media.backend.ffmpeg;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.api.client.media.VideoFrame;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@SideOnly(Side.CLIENT)
public class DirectMemoryVideoDecoder implements IVideoDecoder {
    private String url;
    private int width;
    private int height;
    private double duration = -1;
    private double frameRate = 30.0;
    private volatile boolean error = false;
    private volatile String errorMessage = null;
    private ByteBuffer[] frameBuffers;
    private int frameSize;
    private int maxFrames = 30;
    private int head = 0;
    private int tail = 0;
    private volatile int size = 0;
    private double[] ptsBuffer;
    private VideoFrame currentFrameObj;
    private DecoderThread thread;
    @Override
    public void init(String url) throws Exception {
        this.url = url;
        if (MediaManager.getFfmpegExecutable() == null || !MediaManager.getFfmpegExecutable().exists()) {
            throw new Exception("FFmpeg executable not found.");
        }
        try {
            java.util.List<String> probeCmd = new java.util.ArrayList<>();
            probeCmd.add(MediaManager.getFfmpegExecutable().getAbsolutePath());
            String inputArg = sanitizeFfmpegInput(url, probeCmd);
            probeCmd.add("-i");
            probeCmd.add(inputArg);
            Process p = new ProcessBuilder(probeCmd).redirectErrorStream(true).start();
            String output = IOUtils.toString(p.getInputStream(), "UTF-8");
            if (!p.waitFor(15, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new Exception("FFmpeg probe timed out");
            }
            if (output.contains("Video:")) {
                String[] parts = output.split("Video:")[1].split(",");
                for (String part : parts) {
                    if (part.contains("x")) {
                        String clean = part.trim().split(" ")[0];
                        if (clean.matches("\\d+x\\d+")) {
                            String[] dims = clean.split("x");
                            this.width = Integer.parseInt(dims[0]);
                            this.height = Integer.parseInt(dims[1]);
                        }
                    }
                    if (part.contains("fps")) {
                        String fpsStr = part.trim().split(" ")[0];
                        try { this.frameRate = Double.parseDouble(fpsStr); } catch (Exception e) {}
                    }
                }
            }
            if (output.contains("Duration:")) {
                String durStr = output.split("Duration:")[1].split(",")[0].trim();
                String[] time = durStr.split(":");
                if (time.length == 3) {
                    double hours = Double.parseDouble(time[0]);
                    double mins = Double.parseDouble(time[1]);
                    double secs = Double.parseDouble(time[2]);
                    this.duration = hours * 3600 + mins * 60 + secs;
                }
            }
        } catch (Exception e) {
            setError("Failed to probe video: " + e.getMessage());
            return;
        }
        if (width <= 0 || height <= 0) {
            setError("Could not determine video dimensions");
            return;
        }
        if (width > 1280) {
            double aspect = (double) width / height;
            width = 1280;
            height = (int) (width / aspect);
            if (height % 2 != 0) height++;
        }
        frameSize = width * height * 4;
        frameBuffers = new ByteBuffer[maxFrames];
        for (int i = 0; i < maxFrames; i++) {
            frameBuffers[i] = ByteBuffer.allocateDirect(frameSize).order(ByteOrder.LITTLE_ENDIAN);
        }
        ptsBuffer = new double[maxFrames];
        currentFrameObj = new VideoFrame(width, height);
        currentFrameObj.pixels = null;
        thread = new DecoderThread();
        thread.setDaemon(true);
    }
    public ByteBuffer getDirectBufferFrame() {
        synchronized (this) {
            if (size > 0 && frameBuffers != null) {
                return frameBuffers[head];
            }
        }
        return null;
    }
    @Override
    public double getDuration() {
        return duration;
    }
    @Override
    public boolean isError() {
        return error;
    }
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
    private static String sanitizeFfmpegInput(String input, java.util.List<String> cmd) throws java.io.IOException {
        if (input == null || input.trim().isEmpty()) {
            throw new java.io.IOException("Empty media input");
        }
        String trimmed = input.trim();
        String lower = trimmed.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            com.x4yi.x4ui.impl.client.resource.network.SecurityUtils.validateHttpUrl(trimmed);
            return trimmed;
        }
        File f = new File(trimmed).getCanonicalFile();
        cmd.add("-protocol_whitelist");
        cmd.add("file");
        return "file:" + f.getAbsolutePath();
    }

    @Override
    public void close() {
        DecoderThread t = this.thread;
        this.thread = null;
        if (t != null) {
            t.stopAndJoin(1500);
        }
        synchronized (this) {
            size = 0;
            head = 0;
            tail = 0;
            if (frameBuffers != null) {
                for (int i = 0; i < frameBuffers.length; i++) {
                    if (frameBuffers[i] != null) {
                        cleanDirectBuffer(frameBuffers[i]);
                        frameBuffers[i] = null;
                    }
                }
                frameBuffers = null;
            }
        }
    }
    private static void cleanDirectBuffer(ByteBuffer buffer) {
        com.x4yi.x4ui.common.platform.PlatformBridge.cleanDirectBuffer(buffer);
    }
    @Override
    public int getVideoWidth() {
        return width;
    }
    @Override
    public int getVideoHeight() {
        return height;
    }
    @Override
    public VideoFrame getNextFrame(long currentTimeMs) {
        double currentSec = currentTimeMs / 1000.0;
        boolean found = false;
        double framePts = 0.0;
        synchronized (this) {
            if (size == 0) return null;
            while (size > 0) {
                if (ptsBuffer[head] <= currentSec) {
                    found = true;
                    if (size > 1 && ptsBuffer[(head + 1) % maxFrames] <= currentSec) {
                        head = (head + 1) % maxFrames;
                        size--;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (found) {
                framePts = ptsBuffer[head];
            }
        }
        if (found) {
            currentFrameObj.ready = true;
            currentFrameObj.pts = framePts;
            return currentFrameObj;
        }
        return null;
    }
    public void popFrame() {
        synchronized (this) {
            if (size > 0) {
                head = (head + 1) % maxFrames;
                size--;
            }
        }
    }
    private volatile double startSec = 0.0;
    @Override
    public void seek(double timestampSec) {
        this.startSec = Math.max(0, timestampSec);
        DecoderThread old = this.thread;
        boolean wasAlive = old != null && old.isAlive();
        if (wasAlive) {
            old.stopAndJoin(1500);
        }
        synchronized (this) {
            this.head = 0;
            this.tail = 0;
            this.size = 0;
        }
        if (wasAlive) {
            thread = new DecoderThread(startSec);
            thread.setDaemon(true);
            thread.start();
        }
    }
    @Override
    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new DecoderThread(startSec);
            thread.setDaemon(true);
            thread.start();
        }
    }
    @Override
    public void pause() { }
    protected void setError(String msg) {
        this.error = true;
        this.errorMessage = msg;
    }
    private class DecoderThread extends Thread {
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final double seekOffset;
        private volatile Process process;
        public DecoderThread() {
            this(0.0);
        }
        public DecoderThread(double seekOffset) {
            this.seekOffset = seekOffset;
            this.setName("X4UI-DirectVideoDecoder");
        }
        public void stopThread() {
            running.set(false);
            Process p = this.process;
            if (p != null) {
                p.destroy();
            }
            this.interrupt();
        }
        public void stopAndJoin(long timeoutMs) {
            stopThread();
            if (this.isAlive() && Thread.currentThread() != this) {
                try {
                    this.join(timeoutMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (this.isAlive()) {
                    Process p = this.process;
                    if (p != null) {
                        p.destroyForcibly();
                    }
                    try {
                        this.join(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        @Override
        public void run() {
            try {
                java.util.List<String> cmd = new java.util.ArrayList<>();
                cmd.add(MediaManager.getFfmpegExecutable().getAbsolutePath());
                cmd.add("-loglevel"); cmd.add("quiet");
                if (seekOffset > 0.05) {
                    cmd.add("-ss");
                    cmd.add(String.format(java.util.Locale.US, "%.3f", seekOffset));
                }
                String inputArg = sanitizeFfmpegInput(url, cmd);
                cmd.add("-i"); cmd.add(inputArg);
                cmd.add("-f"); cmd.add("rawvideo");
                cmd.add("-pix_fmt"); cmd.add("bgra");
                cmd.add("-s"); cmd.add(width + "x" + height);
                cmd.add("-an");
                cmd.add("-");
                process = new ProcessBuilder(cmd).start();
                java.nio.channels.ReadableByteChannel channel = java.nio.channels.Channels.newChannel(process.getInputStream());
                int frameIndex = (int) (seekOffset * frameRate);
                while (running.get()) {
                    boolean full;
                    int currentTail;
                    ByteBuffer targetBuf;
                    synchronized (DirectMemoryVideoDecoder.this) {
                        full = (size == maxFrames);
                        currentTail = tail;
                        targetBuf = (!full && frameBuffers != null && currentTail < frameBuffers.length) ? frameBuffers[currentTail] : null;
                    }
                    if (full) {
                        Thread.sleep(5);
                        continue;
                    }
                    if (targetBuf == null) break;
                    targetBuf.clear();
                    while (targetBuf.hasRemaining() && running.get()) {
                        int r = channel.read(targetBuf);
                        if (r == -1) break;
                    }
                    if (!running.get() || targetBuf.hasRemaining()) break;
                    targetBuf.flip();
                    synchronized (DirectMemoryVideoDecoder.this) {
                        ptsBuffer[currentTail] = frameIndex / frameRate;
                        tail = (tail + 1) % maxFrames;
                        size++;
                    }
                    frameIndex++;
                }
            } catch (Exception e) {
                if (running.get()) {
                    org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("[X4UI Video] FFmpeg process error: {}", e.getMessage());
                    String msg = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : e.getClass().getSimpleName();
                    setError("FFmpeg Process Error: " + msg);
                }
            } finally {
                Process p = this.process;
                if (p != null) {
                    p.destroy();
                    if (p.isAlive()) {
                        p.destroyForcibly();
                    }
                }
            }
        }
    }
}