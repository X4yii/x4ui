package com.x4yi.x4ui.impl.client.media.backend.ffmpeg;
import com.x4yi.x4ui.api.client.media.IAudioDecoder;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import com.x4yi.x4ui.impl.client.media.backend.OpenALAudioSink;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
public class FFmpegAudioDecoder implements IAudioDecoder {
    private String url;
    private OpenALAudioSink audioSink;
    private AudioDecoderThread thread;
    private double duration = -1;
    private boolean error = false;
    private String errorMessage = null;
    private float volume = 1.0f;
    private boolean muted = false;
    @Override
    public void init(String url) throws Exception {
        this.url = url;
        if (MediaManager.getFfmpegExecutable() == null || !MediaManager.getFfmpegExecutable().exists()) {
            throw new Exception("FFmpeg executable not found.");
        }
        audioSink = new OpenALAudioSink();
        audioSink.init(48000, 2, 16);
        audioSink.setVolume(muted ? 0.0f : volume);
        thread = new AudioDecoderThread(startSec);
        thread.setDaemon(true);
    }
    private volatile double startSec = 0.0;
    @Override
    public void seek(double timestampSec) {
        this.startSec = Math.max(0, timestampSec);
        if (audioSink != null) {
            audioSink.flush(startSec);
        }
        if (thread != null && thread.isAlive()) {
            thread.stopThread();
            thread = new AudioDecoderThread(startSec);
            thread.setDaemon(true);
            thread.start();
        }
    }
    @Override
    public void start() {
        if (thread == null || !thread.isAlive()) {
            thread = new AudioDecoderThread(startSec);
            thread.setDaemon(true);
            thread.start();
        }
        if (audioSink != null) {
            audioSink.start();
        }
    }
    @Override
    public void pause() {
        if (audioSink != null) {
            audioSink.pause();
        }
    }
    @Override
    public void close() {
        if (thread != null) {
            thread.stopThread();
            thread = null;
        }
        if (audioSink != null) {
            audioSink.close();
            audioSink = null;
        }
    }
    @Override
    public long getCurrentAudioTimeMs() {
        if (audioSink != null) {
            return audioSink.getPlaybackTimeMs();
        }
        return -1;
    }
    @Override
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        if (audioSink != null) {
            audioSink.setVolume(muted ? 0.0f : this.volume);
        }
    }
    @Override
    public float getVolume() {
        return this.volume;
    }
    @Override
    public void setMuted(boolean muted) {
        this.muted = muted;
        if (audioSink != null) {
            audioSink.setVolume(muted ? 0.0f : this.volume);
        }
    }
    @Override
    public boolean isMuted() {
        return this.muted;
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
    protected void setError(String msg) {
        this.error = true;
        this.errorMessage = msg;
    }
    private class AudioDecoderThread extends Thread {
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final double seekOffset;
        private Process process;
        public AudioDecoderThread(double seekOffset) {
            this.seekOffset = seekOffset;
            this.setName("X4UI-FFmpegAudioDecoder");
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
                cmd.add("-f"); cmd.add("s16le");
                cmd.add("-acodec"); cmd.add("pcm_s16le");
                cmd.add("-ac"); cmd.add("2");
                cmd.add("-ar"); cmd.add("48000");
                cmd.add("-vn");
                cmd.add("-");
                process = new ProcessBuilder(cmd).start();
                InputStream is = process.getInputStream();
                byte[] buffer = new byte[8192];
                while (running.get()) {
                    int r = is.read(buffer, 0, buffer.length);
                    if (r == -1) break;
                    if (audioSink != null) {
                        audioSink.writePCM(buffer, 0, r);
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    setError("FFmpeg Audio Process Error: " + e.getMessage());
                }
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }
}