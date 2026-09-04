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
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        int w = this.width;
        int h = this.height;
        int s = this.sliceSize;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bb = tessellator.getBuffer();
        bb.begin(7, DefaultVertexFormats.POSITION_TEX);

        if (s * 2 > w || s * 2 > h) {
            addQuad(bb, absX, absY, w, h, 0, 0, 1, 1);
            tessellator.draw();
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

        // 4 corners
        addQuad(bb, absX, absY, s, s, u1, v1, u2, v2);
        addQuad(bb, absX + w - s, absY, s, s, u3, v1, u4, v2);
        addQuad(bb, absX, absY + h - s, s, s, u1, v3, u2, v4);
        addQuad(bb, absX + w - s, absY + h - s, s, s, u3, v3, u4, v4);

        // 4 edges
        addQuad(bb, absX + s, absY, w - (s * 2), s, u2, v1, u3, v2);
        addQuad(bb, absX + s, absY + h - s, w - (s * 2), s, u2, v3, u3, v4);
        addQuad(bb, absX, absY + s, s, h - (s * 2), u1, v2, u2, v3);
        addQuad(bb, absX + w - s, absY + s, s, h - (s * 2), u3, v2, u4, v3);

        // Center
        addQuad(bb, absX + s, absY + s, w - (s * 2), h - (s * 2), u2, v2, u3, v3);

        tessellator.draw();
    }

    private static void addQuad(BufferBuilder bb, int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        bb.pos(x, y + height, 0.0D).tex(u1, v2).endVertex();
        bb.pos(x + width, y + height, 0.0D).tex(u2, v2).endVertex();
        bb.pos(x + width, y, 0.0D).tex(u2, v1).endVertex();
        bb.pos(x, y, 0.0D).tex(u1, v1).endVertex();
    }

    public static void drawTexturedRect(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        addQuad(bufferbuilder, x, y, width, height, u1, v1, u2, v2);
        tessellator.draw();
    }
}