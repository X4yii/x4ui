package com.x4yi.x4ui.client.gui.component.sprite;
import net.minecraft.util.ResourceLocation;
public class SpriteSheetSource implements ISpriteSource {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int frameWidth;
    private final int frameHeight;
    private final int totalFrames;
    private final int startX;
    private final int startY;
    private final int columns;
    public SpriteSheetSource(ResourceLocation texture, int textureWidth, int textureHeight, int frameWidth, int frameHeight) {
        this(texture, textureWidth, textureHeight, frameWidth, frameHeight, -1, 0, 0);
    }
    public SpriteSheetSource(ResourceLocation texture, int textureWidth, int textureHeight, int frameWidth, int frameHeight, int totalFrames) {
        this(texture, textureWidth, textureHeight, frameWidth, frameHeight, totalFrames, 0, 0);
    }
    public SpriteSheetSource(ResourceLocation texture, int textureWidth, int textureHeight, int frameWidth, int frameHeight, int totalFrames, int startX, int startY) {
        this.texture = texture;
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
        this.frameWidth = Math.max(1, frameWidth);
        this.frameHeight = Math.max(1, frameHeight);
        this.startX = Math.max(0, startX);
        this.startY = Math.max(0, startY);
        int cols = (this.textureWidth - this.startX) / this.frameWidth;
        this.columns = Math.max(1, cols);
        int maxPossibleFrames = this.columns * Math.max(1, (this.textureHeight - this.startY) / this.frameHeight);
        this.totalFrames = (totalFrames > 0 && totalFrames <= maxPossibleFrames) ? totalFrames : maxPossibleFrames;
    }
    @Override
    public int getFrameCount() {
        return totalFrames;
    }
    @Override
    public ResourceLocation getTexture(int frameIndex) {
        return texture;
    }
    @Override
    public void getUV(int frameIndex, float[] outUv) {
        int idx = (totalFrames <= 0) ? 0 : Math.max(0, Math.min(frameIndex, totalFrames - 1));
        int col = idx % columns;
        int row = idx / columns;
        outUv[0] = startX + col * frameWidth;
        outUv[1] = startY + row * frameHeight;
        outUv[2] = frameWidth;
        outUv[3] = frameHeight;
    }
    @Override
    public int getTextureWidth(int frameIndex) {
        return textureWidth;
    }
    @Override
    public int getTextureHeight(int frameIndex) {
        return textureHeight;
    }
}