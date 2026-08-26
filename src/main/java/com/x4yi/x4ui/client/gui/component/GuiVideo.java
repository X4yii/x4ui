package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.renderer.GlStateManager;
public class GuiVideo extends GuiImage {
    private boolean playing = false;
    private String videoUrl;
    public GuiVideo(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public GuiVideo setVideoUrl(String url) {
        this.videoUrl = url;
        return this;
    }
    public void play() {
        this.playing = true;
    }
    public void pause() {
        this.playing = false;
    }
    @Override
    protected void drawSelf(int mouseX, int mouseY, float partialTicks) {
        super.drawSelf(mouseX, mouseY, partialTicks);
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        net.minecraft.client.gui.Gui.drawRect(absX, absY, absX + width, absY + height, 0x66000000);
        int barHeight = 4;
        net.minecraft.client.gui.Gui.drawRect(absX, absY + height - barHeight, absX + width, absY + height, 0xAA000000);
        int progressWidth = (int)((System.currentTimeMillis() % 10000) / 10000.0 * width);
        if (!playing) progressWidth = 0;
        net.minecraft.client.gui.Gui.drawRect(absX, absY + height - barHeight, absX + progressWidth, absY + height, 0xFFFF0000);
        int cx = absX + width / 2;
        int cy = absY + height / 2;
        if (!playing) {
            net.minecraft.client.gui.Gui.drawRect(cx - 5, cy - 8, cx - 2, cy + 8, 0xFFFFFFFF);
            net.minecraft.client.gui.Gui.drawRect(cx - 2, cy - 5, cx + 1, cy + 5, 0xFFFFFFFF);
            net.minecraft.client.gui.Gui.drawRect(cx + 1, cy - 2, cx + 4, cy + 2, 0xFFFFFFFF);
        } else {
            net.minecraft.client.gui.Gui.drawRect(cx - 6, cy - 6, cx - 2, cy + 6, 0xFFFFFFFF);
            net.minecraft.client.gui.Gui.drawRect(cx + 2, cy - 6, cx + 6, cy + 6, 0xFFFFFFFF);
        }
        GlStateManager.enableTexture2D();
        fontRenderer.drawStringWithShadow("VIDEO COMPONENT API (MOCK)", absX + 5, absY + 5, 0xFFFFFF);
        if (videoUrl != null) {
            fontRenderer.drawStringWithShadow(videoUrl, absX + 5, absY + 15, 0xAAAAAA);
        }
    }
    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            playing = !playing;
            return true;
        }
        return false;
    }
}