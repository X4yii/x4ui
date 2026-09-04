package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
public class GuiSprite extends GuiComponent {
    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private int sliceSize = 4;
    public GuiSprite(int x, int y, int width, int height, ResourceLocation texture, int textureWidth, int textureHeight) {
        super(x, y, width, height);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }
    public GuiSprite setSliceSize(int size) {
        this.sliceSize = size;
        return this;
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        if (texture == null) return;
        mc.getTextureManager().bindTexture(texture);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        int w = this.width;
        int h = this.height;
        int s = this.sliceSize;
        if (s * 2 > w || s * 2 > h) {
            drawTexturedRect(absX, absY, w, h, 0, 0, 1, 1);
            return;
        }
        float u1 = 0;
        float u2 = (float) s / textureWidth;
        float u3 = (float) (textureWidth - s) / textureWidth;
        float u4 = 1.0f;
        float v1 = 0;
        float v2 = (float) s / textureHeight;
        float v3 = (float) (textureHeight - s) / textureHeight;
        float v4 = 1.0f;
        drawTexturedRect(absX, absY, s, s, u1, v1, u2, v2);
        drawTexturedRect(absX + w - s, absY, s, s, u3, v1, u4, v2);
        drawTexturedRect(absX, absY + h - s, s, s, u1, v3, u2, v4);
        drawTexturedRect(absX + w - s, absY + h - s, s, s, u3, v3, u4, v4);
        drawTexturedRect(absX + s, absY, w - (s * 2), s, u2, v1, u3, v2);
        drawTexturedRect(absX + s, absY + h - s, w - (s * 2), s, u2, v3, u3, v4);
        drawTexturedRect(absX, absY + s, s, h - (s * 2), u1, v2, u2, v3);
        drawTexturedRect(absX + w - s, absY + s, s, h - (s * 2), u3, v2, u4, v3);
        drawTexturedRect(absX + s, absY + s, w - (s * 2), h - (s * 2), u2, v2, u3, v3);
    }
    public static void drawTexturedRect(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(u1, v2).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(u2, v2).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(u2, v1).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(u1, v1).endVertex();
        tessellator.draw();
    }
}