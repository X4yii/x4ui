package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import com.x4yi.x4ui.impl.client.media.MediaPlayer;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.api.client.media.VideoFrame;
import com.x4yi.x4ui.impl.client.media.backend.ffmpeg.FFmpegProcessDecoder;
import com.x4yi.x4ui.impl.client.media.backend.jcodec.JCodecVideoDecoder;
public class GuiVideo extends GuiComponent {
    private String videoUrl;
    private int glTextureId = -1;
    private boolean hasUploadedFirstFrame = false;
    private double lastUploadedPts = -1.0;
    private MediaPlayer mediaPlayer;
    private float videoAlpha = 1.0f;
    private float videoR = 1.0f;
    private float videoG = 1.0f;
    private float videoB = 1.0f;
    private float audioVolume = 1.0f;
    private boolean audioMuted = false;
    private boolean audioEnabled = true;
    private com.x4yi.x4ui.api.client.media.IVideoControlsLayout controlsLayout;
    private boolean controlsEnabled = true;
    private String loadingStatus = null;
    private int loadingProgress = -1;
    public GuiVideo(int x, int y, int width, int height) {
        super(x, y, width, height);
        mediaPlayer = new MediaPlayer();
        setControlsLayout(new com.x4yi.x4ui.client.gui.component.video.DefaultVideoControlsLayout());
    }
    public GuiVideo setVideoUrl(String url) {
        this.videoUrl = url;
        resetVideo();
        return this;
    }
    public String getVideoUrl() {
        return this.videoUrl;
    }
    public GuiVideo setAlpha(float alpha) {
        this.videoAlpha = alpha;
        return this;
    }
    public GuiVideo setColor(float r, float g, float b) {
        this.videoR = r;
        this.videoG = g;
        this.videoB = b;
        return this;
    }
    public GuiVideo setVolume(float volume) {
        this.audioVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (mediaPlayer.getAudioDecoder() != null) {
            mediaPlayer.getAudioDecoder().setVolume(this.audioVolume);
        }
        return this;
    }
    public float getVolume() {
        return this.audioVolume;
    }
    public GuiVideo setMuted(boolean muted) {
        this.audioMuted = muted;
        if (mediaPlayer.getAudioDecoder() != null) {
            mediaPlayer.getAudioDecoder().setMuted(muted);
        }
        return this;
    }
    public boolean isMuted() {
        return this.audioMuted;
    }
    public GuiVideo setAudioEnabled(boolean enabled) {
        this.audioEnabled = enabled;
        return this;
    }
    public boolean isAudioEnabled() {
        return this.audioEnabled;
    }
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    public GuiVideo setControlsEnabled(boolean enabled) {
        this.controlsEnabled = enabled;
        return this;
    }
    public boolean isControlsEnabled() {
        return this.controlsEnabled;
    }
    public GuiVideo setControlsLayout(com.x4yi.x4ui.api.client.media.IVideoControlsLayout layout) {
        if (this.controlsLayout != null) {
            this.controlsLayout.detach();
        }
        this.controlsLayout = layout;
        if (this.controlsLayout != null) {
            this.controlsLayout.attach(this);
        }
        return this;
    }
    public com.x4yi.x4ui.api.client.media.IVideoControlsLayout getControlsLayout() {
        return this.controlsLayout;
    }
    public void setLoadingState(String status, int progressPercent) {
        this.loadingStatus = status;
        this.loadingProgress = progressPercent;
    }
    public void clearLoadingState() {
        this.loadingStatus = null;
        this.loadingProgress = -1;
    }
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (controlsEnabled && controlsLayout != null) {
            controlsLayout.update(deltaTime);
        }
    }
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    public void play() {
        if (!mediaPlayer.isPlaying()) {
            if (mediaPlayer.getVideoDecoder() == null && videoUrl != null) {
                initVideo();
            }
            mediaPlayer.play();
        }
    }
    public void pause() {
        mediaPlayer.pause();
    }
    public void stop() {
        resetVideo();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetVideo();
        if (controlsLayout != null) {
            controlsLayout.detach();
            controlsLayout = null;
        }
    }
    private void resetVideo() {
        mediaPlayer.stop();
        if (glTextureId != -1) {
            final int toDelete = glTextureId;
            glTextureId = -1;
            if (net.minecraft.client.Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                GlStateManager.deleteTexture(toDelete);
            } else {
                net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> GlStateManager.deleteTexture(toDelete));
            }
        }
        hasUploadedFirstFrame = false;
        lastUploadedPts = -1.0;
    }
    private void initVideo() {
        if (videoUrl == null) return;
        setLoadingState("Loading", -1);
        com.x4yi.x4ui.impl.client.media.MediaPipeline.getInstance().obtainMedia("global", videoUrl, (status, percent) -> {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                setLoadingState(status, percent);
            });
        }).whenComplete((file, err) -> {
            if (err != null) {
                net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                    setLoadingState("Error: " + err.getMessage(), -1);
                });
                return;
            }
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(this::clearLoadingState);
            String localPath = file.getAbsolutePath();
            try {
                IVideoDecoder decoder = com.x4yi.x4ui.impl.client.media.MediaManager.isFFmpegAvailable() ?
                        new com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder() :
                        new com.x4yi.x4ui.impl.client.media.backend.jcodec.JCodecVideoDecoder();
                decoder.init(localPath);
                mediaPlayer.setVideoDecoder(decoder);
                if (audioEnabled && com.x4yi.x4ui.impl.client.media.MediaManager.isFFmpegAvailable()) {
                    com.x4yi.x4ui.impl.client.media.backend.ffmpeg.FFmpegAudioDecoder audioDecoder = new com.x4yi.x4ui.impl.client.media.backend.ffmpeg.FFmpegAudioDecoder();
                    audioDecoder.setVolume(audioVolume);
                    audioDecoder.setMuted(audioMuted);
                    audioDecoder.init(localPath);
                    mediaPlayer.setAudioDecoder(audioDecoder);
                }
                net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                    int w = decoder.getVideoWidth();
                    int h = decoder.getVideoHeight();
                    if (w > 0 && h > 0) {
                        if (glTextureId != -1) {
                            GlStateManager.deleteTexture(glTextureId);
                        }
                        glTextureId = GlStateManager.generateTexture();
                        hasUploadedFirstFrame = false;
                        GlStateManager.bindTexture(glTextureId);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    @Override
    public void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        IVideoDecoder decoder = mediaPlayer.getVideoDecoder();
        if (decoder != null && decoder.isError()) {
            gfx.drawRect(absX, absY, width, height, 0xFF000000);
            String err = decoder.getErrorMessage();
            if (err != null) {
                gfx.drawString(err, absX + 5, absY + height / 2.0f - 4, 0xFF5555, true);
            }
            return;
        }
        if (decoder == null && videoUrl == null) {
            gfx.drawRect(absX, absY, width, height, 0xFF000000);
            return;
        }
        if (mediaPlayer.isPlaying() && decoder != null && glTextureId != -1) {
            long currentTimeMs = (long) (mediaPlayer.getPlaybackTimeSec() * 1000.0);
            VideoFrame frame = decoder.getNextFrame(currentTimeMs);
            if (frame != null && (frame.pts != lastUploadedPts || !hasUploadedFirstFrame)) {
                if (decoder instanceof com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder) {
                    com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder dmvd = (com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder) decoder;
                    java.nio.ByteBuffer byteBuffer = dmvd.getDirectBufferFrame();
                    if (byteBuffer != null) {
                        GlStateManager.bindTexture(glTextureId);
                        byteBuffer.position(0);
                        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 
                            decoder.getVideoWidth(), decoder.getVideoHeight(), 
                            GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, 
                            byteBuffer);
                        hasUploadedFirstFrame = true;
                        lastUploadedPts = frame.pts;
                        dmvd.popFrame();
                    }
                } else if (frame.pixels != null) {
                    java.nio.IntBuffer intBuf = java.nio.IntBuffer.wrap(frame.pixels);
                    GlStateManager.bindTexture(glTextureId);
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 
                        decoder.getVideoWidth(), decoder.getVideoHeight(), 
                        GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, 
                        intBuf);
                    hasUploadedFirstFrame = true;
                    lastUploadedPts = frame.pts;
                }
                if (decoder instanceof JCodecVideoDecoder) {
                    ((JCodecVideoDecoder) decoder).popFrame();
                }
            }
        }
        if (hasUploadedFirstFrame && glTextureId != -1) {
            gfx.save();
            gfx.setColor(videoR, videoG, videoB, videoAlpha);
            GlStateManager.bindTexture(glTextureId);
            Gui.drawModalRectWithCustomSizedTexture(absX, absY, 0, 0, width, height, width, height);
            gfx.restore();
            if (controlsEnabled && controlsLayout != null) {
                controlsLayout.render(gfx, mouseX, mouseY, partialTicks);
            }
        } else {
            gfx.drawRect(absX, absY, width, height, 0xFF000000);
            if (loadingStatus != null) {
                String label = (loadingProgress >= 0) ? (loadingStatus + " (" + loadingProgress + "%)") : loadingStatus;
                gfx.drawString(label, absX + 10, absY + height / 2.0f - 8, 0xFF00E5FF, true);
                if (loadingProgress >= 0) {
                    int pBarW = Math.max(20, width - 24);
                    gfx.drawRoundedRect(absX + 10, absY + height / 2 + 6, pBarW, 4, 2, 0x55FFFFFF);
                    int filledW = (int) (pBarW * (Math.max(0, Math.min(100, loadingProgress)) / 100.0f));
                    if (filledW > 0) {
                        gfx.drawRoundedRect(absX + 10, absY + height / 2 + 6, filledW, 4, 2, 0xFF00E5FF);
                    }
                }
            } else if (mediaPlayer.isPlaying()) {
                gfx.drawString("Loading...", absX + 10, absY + height / 2.0f - 4, 0xFFFFFF, true);
            }
        }
    }
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!visible || !enabled) return false;
        if (controlsEnabled && controlsLayout != null) {
            if (controlsLayout.onMouseClick(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (mouseX >= absX && mouseX <= absX + width && mouseY >= absY && mouseY <= absY + height) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                play();
            }
            return true;
        }
        return false;
    }
    @Override
    public boolean onMouseDrag(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (!visible || !enabled) return false;
        if (controlsEnabled && controlsLayout != null) {
            if (controlsLayout.onMouseDrag(mouseX, mouseY, mouseButton, timeSinceLastClick)) {
                return true;
            }
        }
        return super.onMouseDrag(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }
    @Override
    public boolean onMouseRelease(int mouseX, int mouseY, int state) {
        if (!visible || !enabled) return false;
        if (controlsEnabled && controlsLayout != null) {
            if (controlsLayout.onMouseRelease(mouseX, mouseY, state)) {
                return true;
            }
        }
        return super.onMouseRelease(mouseX, mouseY, state);
    }
}