package com.x4yi.x4ui.impl.client.font;
import com.x4yi.x4ui.api.client.font.IFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
@SideOnly(Side.CLIENT)
public class TrueTypeFont implements IFont {
    private final String name;
    private final Font awtFont;
    private final float displayHeight;
    private final boolean antiAlias;
    private final GlyphData[] glyphs = new GlyphData[256];
    private BufferedImage atlasImage;
    private DynamicTexture dynamicTexture;
    private int textureId = -1;
    private static final int ATLAS_SIZE = 1024;
    private static final int[] COLOR_CODES = new int[32];
    static {
        for (int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int r = (i >> 2 & 1) * 170 + j;
            int g = (i >> 1 & 1) * 170 + j;
            int b = (i & 1) * 170 + j;
            if (i == 6) r += 85;
            if (i >= 16) {
                r /= 4;
                g /= 4;
                b /= 4;
            }
            COLOR_CODES[i] = (r & 255) << 16 | (g & 255) << 8 | (b & 255);
        }
    }
    public TrueTypeFont(String name, Font awtFont, float displayHeight, boolean antiAlias) {
        this.name = name;
        this.awtFont = awtFont;
        this.displayHeight = Math.max(4.0f, displayHeight);
        this.antiAlias = antiAlias;
        initAtlas();
    }
    private void initAtlas() {
        float rasterSize = Math.max(36.0f, displayHeight * 2.5f);
        Font rasterFont = awtFont.deriveFont(rasterSize);
        this.atlasImage = new BufferedImage(ATLAS_SIZE, ATLAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        atlasImage.setRGB(0, 0, 0xFFFFFFFF);
        Graphics2D g2d = atlasImage.createGraphics();
        if (antiAlias) {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        g2d.setFont(rasterFont);
        FontMetrics fm = g2d.getFontMetrics();
        int fontAscent = fm.getAscent();
        int fontHeight = fm.getHeight();
        float scale = displayHeight / fontHeight;
        int cellHeight = fontHeight + 4;
        int cellAscent = fontAscent + 2;
        int currentX = 2;
        int currentY = 2;
        for (int c = 0; c < 256; c++) {
            if (c < 32) {
                glyphs[c] = new GlyphData((char) c, 0, Math.round(displayHeight), 0, 0, 0, 0, 0, 0);
                continue;
            }
            int charW = fm.charWidth(c);
            int cellWidth = Math.max(1, charW) + 4;
            if (currentX + cellWidth >= ATLAS_SIZE - 2) {
                currentX = 2;
                currentY += cellHeight + 2;
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString(String.valueOf((char) c), currentX + 2, currentY + cellAscent);
            float u1 = (float) currentX / ATLAS_SIZE;
            float v1 = (float) currentY / ATLAS_SIZE;
            float u2 = (float) (currentX + cellWidth) / ATLAS_SIZE;
            float v2 = (float) (currentY + cellHeight) / ATLAS_SIZE;
            int adv = Math.max(1, Math.round(charW * scale));
            int renderW = Math.round(cellWidth * scale);
            int renderH = Math.round(cellHeight * scale);
            glyphs[c] = new GlyphData((char) c, adv, Math.round(displayHeight), u1, v1, u2, v2, renderW, renderH);
            currentX += cellWidth + 2;
        }
        g2d.dispose();
    }
    private void checkTexture() {
        if (textureId == -1 && atlasImage != null) {
            this.dynamicTexture = new DynamicTexture(atlasImage);
            this.textureId = dynamicTexture.getGlTextureId();
            GlStateManager.bindTexture(textureId);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            this.atlasImage = null;
        }
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public int getHeight() {
        return Math.round(displayHeight);
    }
    @Override
    public int getWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        int width = 0;
        boolean bold = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                i++;
                if (code == 'l') {
                    bold = true;
                } else if (code == 'r' || (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                    bold = false;
                }
                continue;
            }
            GlyphData glyph = (c < 256) ? glyphs[c] : null;
            if (glyph != null) {
                width += glyph.width + (bold ? 1 : 0);
            } else {
                width += VanillaFont.INSTANCE.getWidth(String.valueOf(c));
            }
        }
        return width;
    }
    @Override
    public int draw(String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return 0;
        if (Minecraft.getMinecraft() == null) {
            return getWidth(text);
        }
        checkTexture();
        if (textureId == -1) {
            return VanillaFont.INSTANCE.draw(text, x, y, color, shadow);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.bindTexture(textureId);
        if (shadow) {
            renderPass(text, x + 0.75f, y + 0.75f, color, true);
        }
        float endX = renderPass(text, x, y, color, false);
        GlStateManager.resetColor();
        return Math.round(endX - x);
    }
    private float renderPass(String text, float x, float y, int initialColor, boolean isShadow) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        int currentColor = initialColor;
        if (isShadow) {
            currentColor = ((currentColor & 0xFCFCFC) >> 2) | (currentColor & 0xFF000000);
        }
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        float currentX = x;
        float underlineStartX = x;
        float strikeStartX = x;
        float a = ((currentColor >> 24) & 0xFF) / 255.0f;
        float r = ((currentColor >> 16) & 0xFF) / 255.0f;
        float g = ((currentColor >> 8) & 0xFF) / 255.0f;
        float b = (currentColor & 0xFF) / 255.0f;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                i++;
                if (code >= '0' && code <= '9') {
                    int idx = code - '0';
                    int rgb = isShadow ? COLOR_CODES[idx + 16] : COLOR_CODES[idx];
                    currentColor = (initialColor & 0xFF000000) | (rgb & 0x00FFFFFF);
                    a = ((currentColor >> 24) & 0xFF) / 255.0f;
                    r = ((currentColor >> 16) & 0xFF) / 255.0f;
                    g = ((currentColor >> 8) & 0xFF) / 255.0f;
                    b = (currentColor & 0xFF) / 255.0f;
                    bold = false; italic = false; underline = false; strikethrough = false;
                } else if (code >= 'a' && code <= 'f') {
                    int idx = code - 'a' + 10;
                    int rgb = isShadow ? COLOR_CODES[idx + 16] : COLOR_CODES[idx];
                    currentColor = (initialColor & 0xFF000000) | (rgb & 0x00FFFFFF);
                    a = ((currentColor >> 24) & 0xFF) / 255.0f;
                    r = ((currentColor >> 16) & 0xFF) / 255.0f;
                    g = ((currentColor >> 8) & 0xFF) / 255.0f;
                    b = (currentColor & 0xFF) / 255.0f;
                    bold = false; italic = false; underline = false; strikethrough = false;
                } else if (code == 'l') {
                    bold = true;
                } else if (code == 'o') {
                    italic = true;
                } else if (code == 'n') {
                    underline = true;
                    underlineStartX = currentX;
                } else if (code == 'm') {
                    strikethrough = true;
                    strikeStartX = currentX;
                } else if (code == 'r') {
                    currentColor = initialColor;
                    if (isShadow) {
                        currentColor = ((currentColor & 0xFCFCFC) >> 2) | (currentColor & 0xFF000000);
                    }
                    a = ((currentColor >> 24) & 0xFF) / 255.0f;
                    r = ((currentColor >> 16) & 0xFF) / 255.0f;
                    g = ((currentColor >> 8) & 0xFF) / 255.0f;
                    b = (currentColor & 0xFF) / 255.0f;
                    bold = false; italic = false; underline = false; strikethrough = false;
                }
                continue;
            }
            GlyphData glyph = (c < 256) ? glyphs[c] : null;
            if (glyph == null) {
                tessellator.draw();
                float advance = VanillaFont.INSTANCE.draw(String.valueOf(c), currentX, y, currentColor, false);
                currentX += advance;
                GlStateManager.bindTexture(textureId);
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                continue;
            }
            float x1 = currentX;
            float x2 = currentX + glyph.renderWidth;
            float y1 = y;
            float y2 = y + glyph.renderHeight;
            float italicOffset = italic ? (glyph.renderHeight * 0.2f) : 0.0f;
            addQuad(buffer, x1 + italicOffset, y1, x2 + italicOffset, y1, x2, y2, x1, y2, glyph.u1, glyph.v1, glyph.u2, glyph.v2, r, g, b, a);
            if (bold) {
                float boldOffset = 0.8f;
                addQuad(buffer, x1 + italicOffset + boldOffset, y1, x2 + italicOffset + boldOffset, y1, x2 + boldOffset, y2, x1 + boldOffset, y2, glyph.u1, glyph.v1, glyph.u2, glyph.v2, r, g, b, a);
            }
            currentX += glyph.width + (bold ? 1 : 0);
            if (underline) {
                float uPixel = 0.5f / ATLAS_SIZE;
                float vPixel = 0.5f / ATLAS_SIZE;
                float lineY = y + displayHeight - 1;
                addQuad(buffer, underlineStartX, lineY, currentX, lineY, currentX, lineY + 1, underlineStartX, lineY + 1, uPixel, vPixel, uPixel, vPixel, r, g, b, a);
                underlineStartX = currentX;
            }
            if (strikethrough) {
                float uPixel = 0.5f / ATLAS_SIZE;
                float vPixel = 0.5f / ATLAS_SIZE;
                float lineY = y + (displayHeight / 2.0f);
                addQuad(buffer, strikeStartX, lineY, currentX, lineY, currentX, lineY + 1, strikeStartX, lineY + 1, uPixel, vPixel, uPixel, vPixel, r, g, b, a);
                strikeStartX = currentX;
            }
        }
        tessellator.draw();
        return currentX;
    }
    private void addQuad(BufferBuilder buffer, float xA, float yA, float xB, float yB, float xC, float yC, float xD, float yD,
                         float u1, float v1, float u2, float v2, float r, float g, float b, float a) {
        buffer.pos(xD, yD, 0.0).tex(u1, v2).color(r, g, b, a).endVertex();
        buffer.pos(xC, yC, 0.0).tex(u2, v2).color(r, g, b, a).endVertex();
        buffer.pos(xB, yB, 0.0).tex(u2, v1).color(r, g, b, a).endVertex();
        buffer.pos(xA, yA, 0.0).tex(u1, v1).color(r, g, b, a).endVertex();
    }
    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        if (text == null || text.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int currentWidth = 0;
        int start = reverse ? text.length() - 1 : 0;
        int step = reverse ? -1 : 1;
        boolean bold = false;
        for (int i = start; i >= 0 && i < text.length(); i += step) {
            char c = text.charAt(i);
            if (!reverse && c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'l') bold = true;
                else if (code == 'r' || (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) bold = false;
                sb.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            } else if (reverse && c != '§' && i > 0 && text.charAt(i - 1) == '§') {
                char code = Character.toLowerCase(c);
                if (code == 'l') bold = true;
                else if (code == 'r' || (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) bold = false;
                sb.append(c).append('§');
                i--;
                continue;
            }
            GlyphData glyph = (c < 256) ? glyphs[c] : null;
            int charW = glyph != null ? (glyph.width + (bold ? 1 : 0)) : VanillaFont.INSTANCE.getWidth(String.valueOf(c));
            if (currentWidth + charW > width) {
                break;
            }
            currentWidth += charW;
            sb.append(c);
        }
        return reverse ? sb.reverse().toString() : sb.toString();
    }
    public void dispose() {
        if (textureId != -1) {
            GlStateManager.deleteTexture(textureId);
            textureId = -1;
        }
        dynamicTexture = null;
        atlasImage = null;
    }
}