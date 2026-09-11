package com.x4yi.x4ui.client.gui.component.sprite;
import net.minecraft.util.ResourceLocation;
import java.util.Arrays;
import java.util.List;
public class SequenceSource implements ISpriteSource {
    private final ResourceLocation[] frames;
    private final int textureWidth;
    private final int textureHeight;
    public SequenceSource(ResourceLocation[] frames, int textureWidth, int textureHeight) {
        this.frames = frames != null ? Arrays.copyOf(frames, frames.length) : new ResourceLocation[0];
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
    }
    public SequenceSource(List<ResourceLocation> frameList, int textureWidth, int textureHeight) {
        this(frameList != null ? frameList.toArray(new ResourceLocation[0]) : new ResourceLocation[0], textureWidth, textureHeight);
    }
    @Override
    public int getFrameCount() {
        return frames.length;
    }
    @Override
    public ResourceLocation getTexture(int frameIndex) {
        if (frames.length == 0) return null;
        int idx = Math.max(0, Math.min(frameIndex, frames.length - 1));
        return frames[idx];
    }
    @Override
    public void getUV(int frameIndex, float[] outUv) {
        outUv[0] = 0.0f;
        outUv[1] = 0.0f;
        outUv[2] = textureWidth;
        outUv[3] = textureHeight;
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