package com.x4yi.x4ui.impl.client.font;
import com.x4yi.x4ui.api.client.font.IFont;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class VanillaFont implements IFont {
    public static final VanillaFont INSTANCE = new VanillaFont();
    private VanillaFont() {}
    private FontRenderer getFontRenderer() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc != null ? mc.fontRenderer : null;
    }
    @Override
    public String getName() {
        return "vanilla";
    }
    @Override
    public int getHeight() {
        FontRenderer fr = getFontRenderer();
        return fr != null ? fr.FONT_HEIGHT : 9;
    }
    @Override
    public int getWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        FontRenderer fr = getFontRenderer();
        if (fr != null) {
            return fr.getStringWidth(text);
        }
        return stripControlCodes(text).length() * 6;
    }
    @Override
    public int draw(String text, float x, float y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return 0;
        FontRenderer fr = getFontRenderer();
        if (fr != null) {
            return fr.drawString(text, x, y, color, shadow);
        }
        return getWidth(text);
    }
    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        if (text == null || text.isEmpty()) return "";
        FontRenderer fr = getFontRenderer();
        if (fr != null) {
            return fr.trimStringToWidth(text, width, reverse);
        }
        int maxChars = Math.max(0, width / 6);
        if (text.length() <= maxChars) return text;
        return reverse ? text.substring(text.length() - maxChars) : text.substring(0, maxChars);
    }
    @Override
    public void drawSplit(String text, float x, float y, int wrapWidth, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return;
        FontRenderer fr = getFontRenderer();
        if (fr != null) {
            fr.drawSplitString(text, (int) x, (int) y, wrapWidth, color);
            return;
        }
        IFont.super.drawSplit(text, x, y, wrapWidth, color, shadow);
    }
    private static String stripControlCodes(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)§[0-9A-FK-OR]", "");
    }
}