package com.x4yi.x4ui.impl.client.media.backend.ffmpeg;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.api.client.media.VideoFrame;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;
public class DirectMemoryVideoDecoder implements IVideoDecoder {
    private String url;
    private int width;
    private int height;
    private double duration = -1;
    private double frameRate = 30.0;
    private boolean error = false;
    private String errorMessage = null;
    private ByteBuffer[] frameBuffers;
    private int frameSize;
    private int maxFrames = 30;
    private int head = 0;
    private int tail = 0;
    private int size = 0;
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
            Process p = new ProcessBuilder(
                MediaManager.getFfmpegExecutable().getAbsolutePath(),
                "-i", url
            ).redirectErrorStream(true).start();
            String output = IOUtils.toString(p.getInputStream(), "UTF-8");
            p.waitFor();
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
    @Override
    public void close() {
        if (thread != null) {
            thread.stopThread();
            thread = null;
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
        if (size == 0) return null;
        double currentSec = currentTimeMs / 1000.0;
        boolean found = false;
        synchronized (this) {
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
        }
        if (found) {
            currentFrameObj.ready = true;
            currentFrameObj.pts = ptsBuffer[head];
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
        synchronized (this) {
            this.head = 0;
            this.tail = 0;
            this.size = 0;
        }
        if (thread != null && thread.isAlive()) {
            thread.stopThread();
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
        private Process process;
        public DecoderThread() {
            this(0.0);
        }
        public DecoderThread(double seekOffset) {
            this.seekOffset = seekOffset;
            this.setName("X4UI-DirectVideoDecoder");
        }
        public void stopThread() {
            running.set(false);
            if (process != null) {
                process.destroy();
            }
            this.interrupt();
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
                cmd.add("-i"); cmd.add(url);
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
                    synchronized (DirectMemoryVideoDecoder.this) {
                        full = (size == maxFrames);
                    }
                    if (full) {
                        Thread.sleep(5);
                        continue;
                    }
                    int currentTail;
                    synchronized (DirectMemoryVideoDecoder.this) {
                        currentTail = tail;
                    }
                    ByteBuffer targetBuf = (frameBuffers != null && currentTail < frameBuffers.length) ? frameBuffers[currentTail] : null;
                    if (targetBuf == null) break;
                    targetBuf.clear();
                    while (targetBuf.hasRemaining() && running.get()) {
                        int r = channel.read(targetBuf);
                        if (r == -1) break;
                    }
                    if (targetBuf.hasRemaining()) break;
                    targetBuf.flip();
                    synchronized (DirectMemoryVideoDecoder.this) {
                        ptsBuffer[currentTail] = frameIndex / frameRate;
                        tail = (tail + 1) % maxFrames;
                        size++;
                    }
                    frameIndex++;
                }
            } catch (Exception e) {
                org.apache.logging.log4j.LogManager.getLogger("x4ui").warn("[X4UI Video] FFmpeg process error: {}", e.getMessage());
                if (running.get()) {
                    String msg = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : e.getClass().getSimpleName();
                    setError("FFmpeg Process Error: " + msg);
                }
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }
}