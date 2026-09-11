package com.x4yi.x4ui.client.gui.component.image;
import com.x4yi.x4ui.client.gui.utils.Insets;
public final class NineSliceConfig {
    private final Insets textureInsets;
    private final Insets borderInsets;
    private final int textureWidth;
    private final int textureHeight;
    private final boolean fillCenter;
    public NineSliceConfig(int sliceSize, int textureWidth, int textureHeight) {
        this(new Insets(sliceSize), new Insets(sliceSize), textureWidth, textureHeight, true);
    }
    public NineSliceConfig(Insets insets, int textureWidth, int textureHeight) {
        this(insets, calculateDefaultBorder(insets, textureWidth, textureHeight), textureWidth, textureHeight, true);
    }
    public NineSliceConfig(Insets textureInsets, int borderSize, int textureWidth, int textureHeight) {
        this(textureInsets, new Insets(borderSize), textureWidth, textureHeight, true);
    }
    public NineSliceConfig(int sliceSize, int borderSize, int textureWidth, int textureHeight) {
        this(new Insets(sliceSize), new Insets(borderSize), textureWidth, textureHeight, true);
    }
    public NineSliceConfig(Insets textureInsets, Insets borderInsets, int textureWidth, int textureHeight) {
        this(textureInsets, borderInsets, textureWidth, textureHeight, true);
    }
    public NineSliceConfig(Insets textureInsets, Insets borderInsets, int textureWidth, int textureHeight, boolean fillCenter) {
        this.textureInsets = textureInsets != null ? textureInsets : Insets.ZERO;
        this.borderInsets = borderInsets != null ? borderInsets : calculateDefaultBorder(this.textureInsets, textureWidth, textureHeight);
        this.textureWidth = Math.max(1, textureWidth);
        this.textureHeight = Math.max(1, textureHeight);
        this.fillCenter = fillCenter;
    }
    private static Insets calculateDefaultBorder(Insets insets, int texWidth, int texHeight) {
        if (insets == null) return Insets.ZERO;
        if (texWidth > 256 || texHeight > 256) {
            float scaleX = texWidth > 256 ? 256.0f / texWidth : 1.0f;
            float scaleY = texHeight > 256 ? 256.0f / texHeight : 1.0f;
            int left = Math.max(1, Math.round(insets.left * scaleX));
            int right = Math.max(1, Math.round(insets.right * scaleX));
            int top = Math.max(1, Math.round(insets.top * scaleY));
            int bottom = Math.max(1, Math.round(insets.bottom * scaleY));
            return new Insets(top, right, bottom, left);
        }
        return insets;
    }
    public NineSliceConfig withBorder(int borderSize) {
        return new NineSliceConfig(this.textureInsets, new Insets(borderSize), this.textureWidth, this.textureHeight, this.fillCenter);
    }
    public NineSliceConfig withBorder(Insets borderInsets) {
        return new NineSliceConfig(this.textureInsets, borderInsets, this.textureWidth, this.textureHeight, this.fillCenter);
    }
    public NineSliceConfig withFillCenter(boolean fillCenter) {
        return new NineSliceConfig(this.textureInsets, this.borderInsets, this.textureWidth, this.textureHeight, fillCenter);
    }
    public Insets getTextureInsets() {
        return textureInsets;
    }
    public Insets getBorderInsets() {
        return borderInsets;
    }
    public boolean isFillCenter() {
        return fillCenter;
    }
    @Deprecated
    public Insets getInsets() {
        return textureInsets;
    }
    public int getTextureWidth() {
        return textureWidth;
    }
    public int getTextureHeight() {
        return textureHeight;
    }
}