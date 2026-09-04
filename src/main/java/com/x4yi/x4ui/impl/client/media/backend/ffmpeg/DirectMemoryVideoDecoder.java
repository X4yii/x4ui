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
    
    // Native Memory buffer to avoid Java Heap (Zero-Copy)
    private ByteBuffer directBuffer;
    private int frameSize;
    private int maxFrames = 30; // 30 frames buffer
    
    // Ring buffer state
    private int head = 0;
    private int tail = 0;
    private int size = 0;
    
    // PTS array parallel to the ring buffer
    private double[] ptsBuffer;
    
    // Extracted frame object to pass to OpenGL (reuses same object, updates position)
    private VideoFrame currentFrameObj;
    
    private DecoderThread thread;

    @Override
    public void init(String url) throws Exception {
        this.url = url;
        
        if (MediaManager.getFfmpegExecutable() == null || !MediaManager.getFfmpegExecutable().exists()) {
            throw new Exception("FFmpeg executable not found.");
        }
        
        // Probe
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
        
        // Quality preservation: allow up to 1280x720, scale down if larger to prevent extreme native RAM usage
        if (width > 1280) {
            double aspect = (double) width / height;
            width = 1280;
            height = (int) (width / aspect);
            if (height % 2 != 0) height++;
        }
        
        frameSize = width * height * 4;
        long totalBytes = (long) frameSize * maxFrames;
        
        // Allocate direct native memory (outside Java Heap)
        directBuffer = ByteBuffer.allocateDirect((int) totalBytes).order(ByteOrder.LITTLE_ENDIAN);
        ptsBuffer = new double[maxFrames];
        currentFrameObj = new VideoFrame(width, height);
        // Remove the internal java heap array since we will use the native buffer directly
        currentFrameObj.pixels = null; 
        
        thread = new DecoderThread();
        thread.setDaemon(true);
    }
    
    // We modify VideoFrame behavior slightly, or we return null and handle it in GuiVideo directly?
    // Wait, GuiVideo expects VideoFrame to have 'pixels' int[]!
    // We will have to update GuiVideo to accept a ByteBuffer instead of an int[].
    
    public ByteBuffer getDirectBufferFrame() {
        return directBuffer; // Position will be set appropriately
    }

    @Override
    public double getDuration() { return duration; }

    @Override
    public boolean isError() { return error; }

    @Override
    public String getErrorMessage() { return errorMessage; }

    @Override
    public void close() {
        if (thread != null) {
            thread.stopThread();
            thread = null;
        }
        size = 0;
        head = 0;
        tail = 0;
    }

    @Override
    public int getVideoWidth() { return width; }

    @Override
    public int getVideoHeight() { return height; }

    @Override
    public VideoFrame getNextFrame(long currentTimeMs) {
        if (size == 0) return null;
        double currentSec = currentTimeMs / 1000.0;
        boolean found = false;
        
        synchronized (this) {
            while (size > 0) {
                if (ptsBuffer[head] <= currentSec) {
                    found = true;
                    // Keep this frame, but pop it if the NEXT frame is also ready?
                    // Actually, if we pop it, it's gone. We want to pop it ONLY when we advance to the next one.
                    // For now, we pop older frames until we find the newest valid one.
                    if (size > 1 && ptsBuffer[(head + 1) % maxFrames] <= currentSec) {
                        head = (head + 1) % maxFrames;
                        size--;
                    } else {
                        break;
                    }
                } else {
                    break; // not reached time yet
                }
            }
        }
        
        if (found) {
            currentFrameObj.ready = true;
            currentFrameObj.pts = ptsBuffer[head];
            
            // Set ByteBuffer position to the correct frame
            directBuffer.position(head * frameSize);
            directBuffer.limit((head * frameSize) + frameSize);
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

    @Override
    public void start() {
        if (thread != null && !thread.isAlive()) {
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
        private Process process;

        public DecoderThread() {
            this.setName("X4UI-DirectVideoDecoder");
        }

        public void stopThread() {
            running.set(false);
            if (process != null) { process.destroy(); }
            this.interrupt();
        }

        @Override
        public void run() {
            try {
                process = new ProcessBuilder(
                    MediaManager.getFfmpegExecutable().getAbsolutePath(),
                    "-loglevel", "quiet",
                    "-i", url,
                    "-f", "rawvideo",
                    "-pix_fmt", "bgra",
                    "-s", width + "x" + height,
                    "-an", "-"
                ).start();

                InputStream is = process.getInputStream();
                byte[] readBuffer = new byte[8192];
                int frameIndex = 0;
                
                // We read directly into the native ByteBuffer using a temporary heap array
                while (running.get()) {
                    boolean full;
                    synchronized (DirectMemoryVideoDecoder.this) { full = (size == maxFrames); }
                    if (full) {
                        Thread.sleep(5);
                        continue;
                    }
                    
                    int currentTail = tail;
                    int startPos = currentTail * frameSize;
                    directBuffer.position(startPos);
                    
                    int bytesRead = 0;
                    while (bytesRead < frameSize) {
                        int toRead = Math.min(readBuffer.length, frameSize - bytesRead);
                        int r = is.read(readBuffer, 0, toRead);
                        if (r == -1) break;
                        directBuffer.put(readBuffer, 0, r);
                        bytesRead += r;
                    }
                    
                    if (bytesRead < frameSize) break;
                    
                    synchronized (DirectMemoryVideoDecoder.this) {
                        ptsBuffer[currentTail] = frameIndex / frameRate;
                        tail = (tail + 1) % maxFrames;
                        size++;
                    }
                    frameIndex++;
                }
            } catch (Exception e) {
                if (running.get()) { setError("FFmpeg Process Error: " + e.getMessage()); }
            } finally {
                if (process != null) { process.destroy(); }
            }
        }
    }
}
