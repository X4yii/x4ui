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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        resetAudio();
    }
    private void resetAudio() {
        mediaPlayer.stop();
    }
    private void initAudio() {
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        IAudioDecoder decoder = mediaPlayer.getAudioDecoder();
        if (decoder != null && decoder.isError()) {
            int absX = getAbsoluteX();
            int absY = getAbsoluteY();
            gfx.drawRect(absX, absY, width, height, 0x88000000);
            String err = decoder.getErrorMessage();
            if (err != null) {
                gfx.drawString(err, absX + 5, absY + height / 2.0f - 4, 0xFF5555, true);
            }
        }
    }
}