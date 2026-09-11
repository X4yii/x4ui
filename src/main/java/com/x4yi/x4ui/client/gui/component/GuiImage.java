package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.gui.IGraphics;
import com.x4yi.x4ui.client.gui.component.image.NineSliceConfig;
import com.x4yi.x4ui.client.gui.utils.Insets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import java.io.File;
import java.util.Locale;
public class GuiImage extends GuiComponent {
    private ResourceLocation texture;
    private String url;
    private float u1 = 0.0f;
    private float v1 = 0.0f;
    private float u2 = 1.0f;
    private float v2 = 1.0f;
    private float alpha = 1.0f;
    private float r = 1.0f;
    private float g = 1.0f;
    private float b = 1.0f;
    private NineSliceConfig nineSliceConfig = null;
    private boolean nineSliceEnabled = false;
    public GuiImage(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public GuiImage(int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height);
        this.texture = texture;
    }
    public GuiImage(int x, int y, int width, int height, String url) {
        super(x, y, width, height);
        setUrl(url);
    }
    public GuiImage withNineSlice(NineSliceConfig config) {
        this.nineSliceConfig = config;
        this.nineSliceEnabled = (config != null);
        return this;
    }
    public GuiImage withNineSlice(int sliceSize, int textureWidth, int textureHeight) {
        return withNineSlice(new NineSliceConfig(sliceSize, textureWidth, textureHeight));
    }
    public GuiImage withNineSlice(Insets insets, int textureWidth, int textureHeight) {
        return withNineSlice(new NineSliceConfig(insets, textureWidth, textureHeight));
    }
    public GuiImage withNineSlice(Insets textureInsets, int borderSize, int textureWidth, int textureHeight) {
        return withNineSlice(new NineSliceConfig(textureInsets, borderSize, textureWidth, textureHeight));
    }
    public GuiImage withNineSlice(Insets textureInsets, Insets borderInsets, int textureWidth, int textureHeight) {
        return withNineSlice(new NineSliceConfig(textureInsets, borderInsets, textureWidth, textureHeight));
    }
    public GuiImage withNineSlice(int sliceSize, int borderSize, int textureWidth, int textureHeight) {
        return withNineSlice(new NineSliceConfig(sliceSize, borderSize, textureWidth, textureHeight));
    }
    public GuiImage setNineSliceEnabled(boolean enabled) {
        this.nineSliceEnabled = enabled;
        return this;
    }
    public boolean isNineSliceEnabled() {
        return nineSliceEnabled && nineSliceConfig != null;
    }
    public NineSliceConfig getNineSliceConfig() {
        return nineSliceConfig;
    }
    public ResourceLocation getResource() {
        return texture;
    }
    public float getAlpha() {
        return alpha;
    }
    public GuiImage setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        return this;
    }
    public GuiImage withAlpha(float alpha) {
        return setAlpha(alpha);
    }
    public GuiImage setColor(float r, float g, float b) {
        this.r = Math.max(0.0f, Math.min(1.0f, r));
        this.g = Math.max(0.0f, Math.min(1.0f, g));
        this.b = Math.max(0.0f, Math.min(1.0f, b));
        return this;
    }
    public GuiImage withColor(float r, float g, float b) {
        return setColor(r, g, b);
    }
    public GuiImage setResource(ResourceLocation location) {
        this.texture = location;
        this.url = null;
        return this;
    }
    public GuiImage withResource(ResourceLocation location) {
        return setResource(location);
    }
    public GuiImage setUrl(String url) {
        this.url = url;
        if (url == null || url.trim().isEmpty()) {
            this.texture = null;
            return this;
        }
        String sanitizedUrl = url.replaceAll("[^a-zA-Z0-9_.-]", "_").toLowerCase(Locale.ROOT);
        if (sanitizedUrl.length() > 64) {
            sanitizedUrl = Integer.toHexString(url.hashCode()) + "_" + sanitizedUrl.substring(sanitizedUrl.length() - 32);
        }
        this.texture = new ResourceLocation("x4ui", "downloaded_images/" + sanitizedUrl);
        if (mc != null && mc.getTextureManager() != null) {
            ITextureObject existing = mc.getTextureManager().getTexture(texture);
            if (existing == null) {
                File cacheDir = new File(Minecraft.getMinecraft().mcDataDir, "x4ui/cache/images");
                if (!cacheDir.exists()) cacheDir.mkdirs();
                File cacheFile = new File(cacheDir, sanitizedUrl + ".png");
                ThreadDownloadImageData image = new ThreadDownloadImageData(cacheFile, url, null, null);
                mc.getTextureManager().loadTexture(texture, image);
            }
        }
        return this;
    }
    public GuiImage withUrl(String url) {
        return setUrl(url);
    }
    public GuiImage setUV(float u1, float v1, float u2, float v2) {
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        return this;
    }
    public GuiImage withUV(float u1, float v1, float u2, float v2) {
        return setUV(u1, v1, u2, v2);
    }
    @Override
    protected void renderSelf(IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (texture != null) {
            int a = (int) (alpha * 255.0f) & 0xFF;
            int red = (int) (r * 255.0f) & 0xFF;
            int grn = (int) (g * 255.0f) & 0xFF;
            int blu = (int) (b * 255.0f) & 0xFF;
            int colorTint = (a << 24) | (red << 16) | (grn << 8) | blu;
            gfx.pushColorTint(colorTint);
            if (nineSliceEnabled && nineSliceConfig != null) {
                gfx.drawNineSlice(
                        texture,
                        absX,
                        absY,
                        width,
                        height,
                        nineSliceConfig.getTextureInsets(),
                        nineSliceConfig.getBorderInsets(),
                        nineSliceConfig.getTextureWidth(),
                        nineSliceConfig.getTextureHeight(),
                        nineSliceConfig.isFillCenter()
                );
            } else {
                gfx.drawTextureRegion(
                        texture,
                        absX,
                        absY,
                        width,
                        height,
                        u1,
                        v1,
                        u2 - u1,
                        v2 - v1,
                        1,
                        1
                );
            }
            gfx.popColorTint();
        } else {
            gfx.drawRect(absX, absY, width, height, 0x88000000);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (texture != null && url != null) {
            Minecraft client = Minecraft.getMinecraft();
            if (client != null && client.getTextureManager() != null) {
                client.getTextureManager().deleteTexture(texture);
            }
            texture = null;
        }
    }
}