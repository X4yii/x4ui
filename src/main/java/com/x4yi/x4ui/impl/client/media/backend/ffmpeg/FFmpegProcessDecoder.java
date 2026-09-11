package com.x4yi.x4ui.impl.client.media.backend.ffmpeg;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import com.x4yi.x4ui.api.client.media.RingBuffer;
import com.x4yi.x4ui.api.client.media.VideoFrame;
import org.apache.commons.io.IOUtils;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
public class FFmpegProcessDecoder implements IVideoDecoder {
    private String url;
    private int width;
    private int height;
    private double duration = -1;
    private double frameRate = 30.0;
    private RingBuffer ringBuffer;
    private DecoderThread thread;
    private boolean error = false;
    private String errorMessage = null;
    private volatile double startSec = 0.0;
    @Override
    public void init(String url) throws Exception {
        this.url = url;
        if (MediaManager.getFfmpegExecutable() == null || !MediaManager.getFfmpegExecutable().exists()) {
            throw new Exception("FFmpeg executable not found. Did MediaManager fail to download?");
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
        if (width > 640) {
            double aspect = (double) width / height;
            width = 640;
            height = (int) (width / aspect);
            if (height % 2 != 0) height++;
        }
        ringBuffer = new RingBuffer(30, width, height);
        thread = new DecoderThread(this, startSec);
        thread.setDaemon(true);
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
        if (ringBuffer != null) {
            ringBuffer.clear();
        }
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
        if (ringBuffer == null || ringBuffer.isEmpty()) return null;
        double currentSec = currentTimeMs / 1000.0;
        VideoFrame frameToRender = null;
        while (!ringBuffer.isEmpty()) {
            VideoFrame head = ringBuffer.getHead();
            if (head != null && head.ready && head.pts <= currentSec) {
                frameToRender = head;
                ringBuffer.popHead();
            } else {
                break;
            }
        }
        return frameToRender;
    }
    @Override
    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new DecoderThread(this, startSec);
            thread.setDaemon(true);
            thread.start();
        }
    }
    @Override
    public void pause() {
    }
    @Override
    public void seek(double timestampSec) {
        this.startSec = Math.max(0, timestampSec);
        if (ringBuffer != null) {
            ringBuffer.clear();
        }
        if (thread != null && thread.isAlive()) {
            thread.stopThread();
            thread = new DecoderThread(this, startSec);
            thread.setDaemon(true);
            thread.start();
        }
    }
    protected void setError(String msg) {
        this.error = true;
        this.errorMessage = msg;
    }
    private class DecoderThread extends Thread {
        private final FFmpegProcessDecoder parent;
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final double seekOffset;
        private Process process;
        public DecoderThread(FFmpegProcessDecoder parent) {
            this(parent, 0.0);
        }
        public DecoderThread(FFmpegProcessDecoder parent, double seekOffset) {
            this.parent = parent;
            this.seekOffset = seekOffset;
            this.setName("X4UI-FFmpegVideoDecoder");
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
                cmd.add("-s"); cmd.add(parent.width + "x" + parent.height);
                cmd.add("-an");
                cmd.add("-");
                process = new ProcessBuilder(cmd).start();
                InputStream is = process.getInputStream();
                int w = parent.width;
                int h = parent.height;
                int frameSize = w * h * 4;
                byte[] rawFrame = new byte[frameSize];
                int frameIndex = (int) (seekOffset * parent.frameRate);
                while (running.get()) {
                    if (parent.ringBuffer.isFull()) {
                        Thread.sleep(5);
                        continue;
                    }
                    int bytesRead = 0;
                    while (bytesRead < frameSize) {
                        int r = is.read(rawFrame, bytesRead, frameSize - bytesRead);
                        if (r == -1) break;
                        bytesRead += r;
                    }
                    if (bytesRead < frameSize) {
                        break;
                    }
                    VideoFrame frame = parent.ringBuffer.getTail();
                    if (frame != null && !frame.ready) {
                        java.nio.ByteBuffer.wrap(rawFrame)
                            .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                            .asIntBuffer()
                            .get(frame.pixels);
                        frame.pts = frameIndex / parent.frameRate;
                        frame.ready = true;
                        parent.ringBuffer.pushTail();
                        frameIndex++;
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    parent.setError("FFmpeg Process Error: " + e.getMessage());
                }
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }
}