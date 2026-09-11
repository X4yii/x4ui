package com.x4yi.x4ui.client.gui.component.sprite;
import net.minecraft.util.ResourceLocation;
public interface ISpriteSource {
    int getFrameCount();
    ResourceLocation getTexture(int frameIndex);
    void getUV(int frameIndex, float[] outUv);
    int getTextureWidth(int frameIndex);
    int getTextureHeight(int frameIndex);
}