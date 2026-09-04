package com.x4yi.x4ui.client.gui.component;

import com.x4yi.x4ui.impl.client.media.MediaPlayer;
import com.x4yi.x4ui.api.client.media.IAudioDecoder;
import net.minecraft.client.gui.Gui;

public class GuiAudio extends GuiComponent {
    private String audioUrl;
    private MediaPlayer mediaPlayer;
    
    public GuiAudio(int x, int y, int width, int height) {
        super(x, y, width, height);
        mediaPlayer = new MediaPlayer();
    }

    public GuiAudio setAudioUrl(String url) {
        this.audioUrl = url;
        resetAudio();
        return this;
    }

    public void play() {
        if (!mediaPlayer.isPlaying()) {
            if (mediaPlayer.getAudioDecoder() == null && audioUrl != null) {
                initAudio();
            }
            mediaPlayer.play();
        }
    }

    public void pause() {
        mediaPlayer.pause();
    }
    
    public void stop() {
        resetAudio();
    }
    
    private void resetAudio() {
        mediaPlayer.stop();
    }
    
    private void initAudio() {
        // Phase 3 will inject the FFmpegAudioDecoder here.
        // For now, this is a stub since JCodec doesn't extract audio.
    }

    @Override
    public void renderSelf(int mouseX, int mouseY, float partialTicks) {
        IAudioDecoder decoder = mediaPlayer.getAudioDecoder();
        
        if (decoder != null && decoder.isError()) {
            // Draw error state
            Gui.drawRect(x, y, x + width, y + height, 0x88000000);
            String err = decoder.getErrorMessage();
            if (err != null) {
                mc.fontRenderer.drawStringWithShadow(err, x + 5, y + height / 2 - 4, 0xFF5555);
            }
        }
    }
}
