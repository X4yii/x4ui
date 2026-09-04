package com.x4yi.x4ui.client.gui.component;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.x4yi.x4ui.impl.client.media.MediaPlayer;
import com.x4yi.x4ui.api.client.media.IVideoDecoder;
import com.x4yi.x4ui.api.client.media.VideoFrame;
import com.x4yi.x4ui.impl.client.media.backend.ffmpeg.FFmpegProcessDecoder;
import com.x4yi.x4ui.impl.client.media.backend.jcodec.JCodecVideoDecoder;

public class GuiVideo extends GuiImage {
    private String videoUrl;
    private DynamicTexture dynamicTexture;
    
    // We instantiate MediaPlayer directly for Phase 1. 
    // In Phase 3, this will come from MediaManager.
    private MediaPlayer mediaPlayer;
    
    private float videoAlpha = 1.0f;
    private float videoR = 1.0f;
    private float videoG = 1.0f;
    private float videoB = 1.0f;
    
    public GuiVideo(int x, int y, int width, int height) {
        super(x, y, width, height);
        mediaPlayer = new MediaPlayer();
    }

    public GuiVideo setVideoUrl(String url) {
        this.videoUrl = url;
        resetVideo();
        return this;
    }

    @Override
    public GuiVideo setAlpha(float alpha) {
        super.setAlpha(alpha);
        this.videoAlpha = alpha;
        return this;
    }

    @Override
    public GuiVideo setColor(float r, float g, float b) {
        super.setColor(r, g, b);
        this.videoR = r;
        this.videoG = g;
        this.videoB = b;
        return this;
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
    
    private void resetVideo() {
        mediaPlayer.stop();
        if (dynamicTexture != null) {
            GlStateManager.deleteTexture(dynamicTexture.getGlTextureId());
            dynamicTexture = null;
        }
    }
    
    private void initVideo() {
        if (videoUrl == null) return;
        
        com.x4yi.x4ui.X4UI.getMediaService().streamAsync(videoUrl, new com.x4yi.x4ui.api.client.media.AssetCallback() {
            @Override
            public void onAssetReady(com.x4yi.x4ui.api.client.media.IMediaAsset asset) {
                try {
                    IVideoDecoder decoder = asset.createDecoder();
                    decoder.init(asset.getIdentifier());
                    
                    mediaPlayer.setVideoDecoder(decoder);
                    
                    if (decoder.getVideoWidth() > 0 && decoder.getVideoHeight() > 0) {
                        dynamicTexture = new DynamicTexture(decoder.getVideoWidth(), decoder.getVideoHeight());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("[X4UI] MediaService Error: " + errorMessage);
            }
        });
    }

    @Override
    public void renderSelf(int mouseX, int mouseY, float partialTicks) {
        IVideoDecoder decoder = mediaPlayer.getVideoDecoder();
        
        if (decoder != null && decoder.isError()) {
            // Draw error state
            drawRect(x, y, x + width, y + height, 0xFF000000);
            String err = decoder.getErrorMessage();
            if (err != null) {
                mc.fontRenderer.drawStringWithShadow(err, x + 5, y + height / 2 - 4, 0xFF5555);
            }
            return;
        }

        if (decoder == null && videoUrl == null) {
            // Draw empty state
            drawRect(x, y, x + width, y + height, 0xFF000000);
            return;
        }
        
        if (mediaPlayer.isPlaying() && decoder != null && dynamicTexture != null) {
            // Poll for next frame based on Master Clock
            long currentTimeMs = (long) (mediaPlayer.getPlaybackTimeSec() * 1000.0);
            VideoFrame frame = decoder.getNextFrame(currentTimeMs);
            
            if (frame != null) {
                // Update texture using Zero-Copy if possible
                if (decoder instanceof com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder) {
                    com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder dmvd = (com.x4yi.x4ui.impl.client.media.backend.ffmpeg.DirectMemoryVideoDecoder) decoder;
                    java.nio.ByteBuffer byteBuffer = dmvd.getDirectBufferFrame();
                    if (byteBuffer != null) {
                        GlStateManager.bindTexture(dynamicTexture.getGlTextureId());
                        org.lwjgl.opengl.GL11.glTexSubImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, 0, 0, 
                            decoder.getVideoWidth(), decoder.getVideoHeight(), 
                            org.lwjgl.opengl.GL12.GL_BGRA, org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV, 
                            byteBuffer.asIntBuffer());
                    }
                } else if (frame.pixels != null) {
                    int[] textureData = dynamicTexture.getTextureData();
                    if (frame.pixels.length <= textureData.length) {
                        System.arraycopy(frame.pixels, 0, textureData, 0, frame.pixels.length);
                        dynamicTexture.updateDynamicTexture();
                    }
                }
                
                // If it's a JCodecVideoDecoder, tell it to pop the frame we just consumed
                if (decoder instanceof JCodecVideoDecoder) {
                    ((JCodecVideoDecoder) decoder).popFrame();
                }
            }
        }

        if (dynamicTexture != null) {
            GlStateManager.color(videoR, videoG, videoB, videoAlpha);
            GlStateManager.bindTexture(dynamicTexture.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        } else {
            drawRect(x, y, x + width, y + height, 0xFF000000);
            mc.fontRenderer.drawStringWithShadow("Loading...", x + 5, y + height / 2 - 4, 0xFFFFFF);
        }
    }
    
    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (super.onMouseClick(mouseX, mouseY, mouseButton)) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                play();
            }
            return true;
        }
        return false;
    }
}