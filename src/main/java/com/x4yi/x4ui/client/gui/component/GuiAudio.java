package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.api.client.media.IAudioDecoder;
import com.x4yi.x4ui.impl.client.media.MediaManager;
import com.x4yi.x4ui.impl.client.media.MediaPipeline;
import com.x4yi.x4ui.impl.client.media.MediaPlayer;
import com.x4yi.x4ui.impl.client.media.backend.ffmpeg.FFmpegAudioDecoder;
import net.minecraft.client.Minecraft;

public class GuiAudio extends GuiComponent {
    private String audioUrl;
    private final MediaPlayer mediaPlayer;
    private float volume = 1.0f;
    private boolean muted = false;
    private volatile boolean loading = false;
    private String loadError = null;

    public GuiAudio(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.mediaPlayer = new MediaPlayer();
    }

    public GuiAudio setAudioUrl(String url) {
        this.audioUrl = url;
        this.loadError = null;
        resetAudio();
        return this;
    }

    public GuiAudio setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        if (mediaPlayer.getAudioDecoder() != null) {
            mediaPlayer.getAudioDecoder().setVolume(this.volume);
        }
        return this;
    }

    public float getVolume() {
        return volume;
    }

    public GuiAudio setMuted(boolean muted) {
        this.muted = muted;
        if (mediaPlayer.getAudioDecoder() != null) {
            mediaPlayer.getAudioDecoder().setMuted(muted);
        }
        return this;
    }

    public boolean isMuted() {
        return muted;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            if (mediaPlayer.getAudioDecoder() == null && audioUrl != null && !loading) {
                initAudio();
            } else if (mediaPlayer.getAudioDecoder() != null) {
                mediaPlayer.play();
            }
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        resetAudio();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetAudio();
    }

    private void resetAudio() {
        loading = false;
        mediaPlayer.stop();
    }

    private void initAudio() {
        if (audioUrl == null || audioUrl.trim().isEmpty()) return;
        if (!MediaManager.isFFmpegAvailable()) {
            loadError = "FFmpeg unavailable for audio";
            return;
        }
        loading = true;
        loadError = null;
        final String requestedUrl = audioUrl;
        MediaPipeline.getInstance().obtainMedia("global", requestedUrl, null).whenComplete((file, err) -> {
            loading = false;
            if (err != null) {
                loadError = err.getMessage();
                return;
            }
            if (!requestedUrl.equals(this.audioUrl)) {
                return;
            }
            try {
                FFmpegAudioDecoder audioDecoder = new FFmpegAudioDecoder();
                audioDecoder.setVolume(volume);
                audioDecoder.setMuted(muted);
                audioDecoder.init(file.getAbsolutePath());
                mediaPlayer.setAudioDecoder(audioDecoder);
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) {
                    mc.addScheduledTask(mediaPlayer::play);
                } else {
                    mediaPlayer.play();
                }
            } catch (Exception e) {
                loadError = e.getMessage();
            }
        });
    }

    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        IAudioDecoder decoder = mediaPlayer.getAudioDecoder();
        String err = loadError;
        if (err == null && decoder != null && decoder.isError()) {
            err = decoder.getErrorMessage();
        }
        if (err != null) {
            int absX = getAbsoluteX();
            int absY = getAbsoluteY();
            gfx.drawRect(absX, absY, width, height, 0x88000000);
            gfx.drawString(err, absX + 5, absY + height / 2.0f - 4, 0xFFFF5555, true);
        }
    }
}
