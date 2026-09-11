package com.x4yi.x4ui.test;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.utils.Insets;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import javax.annotation.Nullable;
import java.util.function.Consumer;
public class HeadlessGraphics implements IGraphics {
    public int drawCallCount = 0;
    public int saveCount = 0;
    public int restoreCount = 0;
    public int depth = 0;
    public float currentAlpha = 1.0f;
    public int currentColorTint = 0xFFFFFFFF;
    private static final int MAX_DEPTH = 32;
    private final float[] alphaStack = new float[MAX_DEPTH];
    private final int[] colorTintStack = new int[MAX_DEPTH];
    private final com.x4yi.x4ui.api.client.font.IFont[] fontStack = new com.x4yi.x4ui.api.client.font.IFont[MAX_DEPTH];
    private final int[] clipStack = new int[MAX_DEPTH * 4];
    public int clipDepth = 0;
    private com.x4yi.x4ui.api.client.font.IFont activeFont = com.x4yi.x4ui.impl.client.font.VanillaFont.INSTANCE;
    public void reset() {
        drawCallCount = 0;
        saveCount = 0;
        restoreCount = 0;
        depth = 0;
        clipDepth = 0;
        currentAlpha = 1.0f;
        currentColorTint = 0xFFFFFFFF;
        activeFont = com.x4yi.x4ui.impl.client.font.VanillaFont.INSTANCE;
    }
    @Override
    public void save() {
        if (depth < MAX_DEPTH - 1) {
            alphaStack[depth] = currentAlpha;
            colorTintStack[depth] = currentColorTint;
            fontStack[depth] = activeFont;
            depth++;
            saveCount++;
        }
    }
    @Override
    public void restore() {
        if (depth > 0) {
            depth--;
            currentAlpha = alphaStack[depth];
            currentColorTint = colorTintStack[depth];
            activeFont = fontStack[depth];
            restoreCount++;
        }
    }
    @Override public void translate(float x, float y) {}
    @Override public void scale(float sx, float sy) {}
    @Override public void scale(float sx, float sy, float originX, float originY) {}
    @Override public void rotate(float angleDegrees) {}
    @Override public void rotate(float angleDegrees, float originX, float originY) {}
    @Override public void setColor(int argb) { drawCallCount++; }
    @Override public void setColor(float r, float g, float b, float a) { drawCallCount++; }
    @Override public void setAlpha(float alpha) { this.currentAlpha = alpha; }
    @Override public void pushAlpha(float alpha) { save(); this.currentAlpha *= alpha; }
    @Override public void popAlpha() { restore(); }
    @Override public void pushColorTint(int argb) { save(); }
    @Override public void popColorTint() { restore(); }
    @Override public void resetColor() { currentAlpha = 1.0f; currentColorTint = 0xFFFFFFFF; }
    @Override public void drawRect(int x, int y, int width, int height, int argb) { drawCallCount++; }
    @Override public void drawRoundedRect(int x, int y, int width, int height, int radius, int argb) { drawCallCount++; }
    @Override public void drawBorder(int x, int y, int width, int height, int borderWidth, int argb) { drawCallCount++; }
    @Override public void drawRoundedBorder(int x, int y, int width, int height, int radius, int borderWidth, int argb) { drawCallCount++; }
    @Override public void drawLine(float x1, float y1, float x2, float y2, float thickness, int argb) { drawCallCount++; }
    @Override public void drawCircle(float centerX, float centerY, float radius, int argb) { drawCallCount++; }
    @Override public void drawGradient(int x, int y, int width, int height, int colorTop, int colorBottom) { drawCallCount++; }
    @Override public void drawGradientHorizontal(int x, int y, int width, int height, int colorLeft, int colorRight) { drawCallCount++; }
    @Override public void drawGradient4Corners(int x, int y, int width, int height, int topLeft, int topRight, int bottomLeft, int bottomRight) { drawCallCount++; }
    @Override public void drawTexture(ResourceLocation texture, int x, int y, int width, int height) { drawCallCount++; }
    @Override public void drawTextureRegion(ResourceLocation texture, int x, int y, int width, int height, float u, float v, float uWidth, float vHeight, int texWidth, int texHeight) { drawCallCount++; }
    @Override public void drawNineSlice(ResourceLocation texture, int x, int y, int width, int height, Insets sliceInsets, int texWidth, int texHeight) { drawCallCount++; }
    @Override public void drawNineSlice(ResourceLocation texture, int x, int y, int width, int height, Insets textureInsets, Insets borderInsets, int texWidth, int texHeight, boolean fillCenter) { drawCallCount++; }
    @Override public void setFont(com.x4yi.x4ui.api.client.font.IFont font) { this.activeFont = font != null ? font : com.x4yi.x4ui.impl.client.font.VanillaFont.INSTANCE; }
    @Override public com.x4yi.x4ui.api.client.font.IFont getFont() { return this.activeFont; }
    @Override public void resetFont() { this.activeFont = com.x4yi.x4ui.impl.client.font.VanillaFont.INSTANCE; }
    @Override public int drawString(String text, float x, float y, int color, boolean shadow) { drawCallCount++; return activeFont != null ? activeFont.draw(text, x, y, color, shadow) : getStringWidth(text); }
    @Override public int drawStringCentered(String text, float centerX, float y, int color, boolean shadow) { drawCallCount++; return activeFont != null ? drawString(text, centerX - getStringWidth(text) / 2.0f, y, color, shadow) : getStringWidth(text); }
    @Override public int drawStringRightAligned(String text, float rightX, float y, int color, boolean shadow) { drawCallCount++; return activeFont != null ? drawString(text, rightX - getStringWidth(text), y, color, shadow) : getStringWidth(text); }
    @Override public void drawSplitString(String text, float x, float y, int wrapWidth, int color, boolean shadow) { drawCallCount++; if (activeFont != null) activeFont.drawSplit(text, x, y, wrapWidth, color, shadow); }
    @Override public int getStringWidth(String text) { return activeFont != null ? activeFont.getWidth(text) : (text == null ? 0 : text.length() * 6); }
    @Override public int getFontHeight() { return activeFont != null ? activeFont.getHeight() : 9; }
    @Override public String trimStringToWidth(String text, int width) { return activeFont != null ? activeFont.trimStringToWidth(text, width) : (text == null ? "" : text); }
    @Override
    public void pushClip(int x, int y, int width, int height) {
        if (clipDepth >= MAX_DEPTH - 1) return;
        int finalX = x;
        int finalY = y;
        int finalW = width;
        int finalH = height;
        if (clipDepth > 0) {
            int parentIdx = (clipDepth - 1) * 4;
            int px = clipStack[parentIdx];
            int py = clipStack[parentIdx + 1];
            int pw = clipStack[parentIdx + 2];
            int ph = clipStack[parentIdx + 3];
            int nx = Math.max(x, px);
            int ny = Math.max(y, py);
            int nx2 = Math.min(x + width, px + pw);
            int ny2 = Math.min(y + height, py + ph);
            finalX = nx;
            finalY = ny;
            finalW = Math.max(0, nx2 - nx);
            finalH = Math.max(0, ny2 - ny);
        }
        int idx = clipDepth * 4;
        clipStack[idx] = finalX;
        clipStack[idx + 1] = finalY;
        clipStack[idx + 2] = finalW;
        clipStack[idx + 3] = finalH;
        clipDepth++;
    }
    @Override
    public void popClip() {
        if (clipDepth > 0) {
            clipDepth--;
        }
    }
    @Override
    public boolean isClipped(int x, int y, int width, int height) {
        if (clipDepth == 0) return false;
        int idx = (clipDepth - 1) * 4;
        int cx = clipStack[idx];
        int cy = clipStack[idx + 1];
        int cw = clipStack[idx + 2];
        int ch = clipStack[idx + 3];
        return x + width <= cx || x >= cx + cw || y + height <= cy || y >= cy + ch;
    }
    public int[] getCurrentClip() {
        if (clipDepth == 0) return null;
        int idx = (clipDepth - 1) * 4;
        return new int[]{clipStack[idx], clipStack[idx + 1], clipStack[idx + 2], clipStack[idx + 3]};
    }
    @Override public void drawItemStack(ItemStack stack, int x, int y) { drawCallCount++; }
    @Override public void drawItemStackWithOverlay(ItemStack stack, int x, int y, @Nullable String altText) { drawCallCount++; }
    @Override public void renderCustom(Consumer<Minecraft> customRenderer) { drawCallCount++; }
}