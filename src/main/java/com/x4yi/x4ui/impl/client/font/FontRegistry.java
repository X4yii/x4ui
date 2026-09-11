package com.x4yi.x4ui.impl.client.font;
import com.x4yi.x4ui.api.client.font.IFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.awt.Font;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@SideOnly(Side.CLIENT)
public class FontRegistry implements IResourceManagerReloadListener {
    public static final FontRegistry INSTANCE = new FontRegistry();
    private final Map<String, IFont> fonts = new ConcurrentHashMap<>();
    private final Map<String, FontSource> sources = new ConcurrentHashMap<>();
    private static class FontSource {
        final ResourceLocation location;
        final float displayHeight;
        final boolean antiAlias;
        FontSource(ResourceLocation location, float displayHeight, boolean antiAlias) {
            this.location = location;
            this.displayHeight = displayHeight;
            this.antiAlias = antiAlias;
        }
    }
    private FontRegistry() {
        fonts.put("vanilla", VanillaFont.INSTANCE);
        fonts.put("default", VanillaFont.INSTANCE);
    }
    public void register(String name, IFont font) {
        if (name == null || font == null) return;
        String key = name.toLowerCase();
        IFont oldFont = fonts.put(key, font);
        if (oldFont instanceof TrueTypeFont && oldFont != font) {
            ((TrueTypeFont) oldFont).dispose();
        }
    }
    public IFont get(String name) {
        if (name == null) return VanillaFont.INSTANCE;
        IFont font = fonts.get(name.toLowerCase());
        return font != null ? font : VanillaFont.INSTANCE;
    }
    public boolean has(String name) {
        return name != null && fonts.containsKey(name.toLowerCase());
    }
    public IFont loadFont(String name, ResourceLocation location, float displayHeight) {
        return loadFont(name, location, displayHeight, true);
    }
    public IFont loadFont(String name, ResourceLocation location, float displayHeight, boolean antiAlias) {
        sources.put(name.toLowerCase(), new FontSource(location, displayHeight, antiAlias));
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.getResourceManager() != null) {
                IResource res = mc.getResourceManager().getResource(location);
                try (InputStream is = res.getInputStream()) {
                    Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is);
                    TrueTypeFont font = new TrueTypeFont(name, awtFont, displayHeight, antiAlias);
                    register(name, font);
                    return font;
                }
            }
        } catch (Exception e) {
            System.err.println("[X4UI] Failed to load font '" + name + "' from " + location + ": " + e.getMessage());
        }
        return VanillaFont.INSTANCE;
    }
    public IFont loadFont(String name, InputStream inputStream, float displayHeight, boolean antiAlias) {
        try {
            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            TrueTypeFont font = new TrueTypeFont(name, awtFont, displayHeight, antiAlias);
            register(name, font);
            return font;
        } catch (Exception e) {
            System.err.println("[X4UI] Failed to load font '" + name + "' from InputStream: " + e.getMessage());
            return VanillaFont.INSTANCE;
        }
    }
    public IFont loadSystemFont(String name, String familyName, int style, float displayHeight, boolean antiAlias) {
        Font awtFont = new Font(familyName, style, Math.round(displayHeight * 2.0f));
        TrueTypeFont font = new TrueTypeFont(name, awtFont, displayHeight, antiAlias);
        register(name, font);
        return font;
    }
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        for (Map.Entry<String, FontSource> entry : sources.entrySet()) {
            String name = entry.getKey();
            FontSource src = entry.getValue();
            IFont oldFont = fonts.get(name);
            if (oldFont instanceof TrueTypeFont) {
                ((TrueTypeFont) oldFont).dispose();
            }
            try {
                IResource res = resourceManager.getResource(src.location);
                try (InputStream is = res.getInputStream()) {
                    Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is);
                    TrueTypeFont font = new TrueTypeFont(name, awtFont, src.displayHeight, src.antiAlias);
                    register(name, font);
                }
            } catch (Exception e) {
                System.err.println("[X4UI] Error reloading font '" + name + "': " + e.getMessage());
            }
        }
    }
}