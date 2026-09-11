package com.x4yi.x4ui.impl.client.font;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class GlyphData {
    public final char character;
    public final int width;
    public final int height;
    public final float u1, v1;
    public final float u2, v2;
    public final int renderWidth;
    public final int renderHeight;
    public GlyphData(char character, int width, int height, float u1, float v1, float u2, float v2, int renderWidth, int renderHeight) {
        this.character = character;
        this.width = width;
        this.height = height;
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        this.renderWidth = renderWidth;
        this.renderHeight = renderHeight;
    }
}