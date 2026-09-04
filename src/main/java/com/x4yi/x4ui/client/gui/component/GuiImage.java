package com.x4yi.x4ui.client.gui.component;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import java.io.File;
public class GuiImage extends GuiComponent {
    private ResourceLocation texture;
    private String url;
    private float u1 = 0, v1 = 0, u2 = 1, v2 = 1;
    private float alpha = 1.0F;
    private float r = 1.0F, g = 1.0F, b = 1.0F;
    public GuiImage(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    public GuiImage setAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }
    public GuiImage setColor(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }
    public GuiImage setResource(ResourceLocation location) {
        this.texture = location;
        this.url = null;
        return this;
    }
    public GuiImage setUrl(String url) {
        this.url = url;
        String sanitizedUrl = url.replaceAll("[^a-zA-Z0-9_.-]", "_").toLowerCase(java.util.Locale.ROOT);
        if (sanitizedUrl.length() > 64) {
            sanitizedUrl = Integer.toHexString(url.hashCode()) + "_" + sanitizedUrl.substring(sanitizedUrl.length() - 32);
        }
        this.texture = new ResourceLocation("x4ui", "downloaded_images/" + sanitizedUrl);
        ITextureObject existing = mc.getTextureManager().getTexture(texture);
        if (existing == null) {
            File cacheDir = new File(Minecraft.getMinecraft().mcDataDir, "x4ui_cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            File cacheFile = new File(cacheDir, sanitizedUrl + ".png");
            ThreadDownloadImageData image = new ThreadDownloadImageData(cacheFile, url, null, null);
            mc.getTextureManager().loadTexture(texture, image);
        }
        return this;
    }
    public GuiImage setUV(float u1, float v1, float u2, float v2) {
        this.u1 = u1;
        this.v1 = v1;
        this.u2 = u2;
        this.v2 = v2;
        return this;
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        if (texture != null) {
            mc.getTextureManager().bindTexture(texture);
            GlStateManager.color(r, g, b, alpha);
            GuiSprite.drawTexturedRect(getAbsoluteX(), getAbsoluteY(), width, height, u1, v1, u2, v2);
        } else {
            net.minecraft.client.gui.Gui.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + width, getAbsoluteY() + height, 0x88000000);
        }
    }
}