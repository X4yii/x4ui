package com.x4yi.x4ui.api.client.font;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public interface IFont {
    String getName();
    int getHeight();
    int getWidth(String text);
    int draw(String text, float x, float y, int color, boolean shadow);
    String trimStringToWidth(String text, int width, boolean reverse);
    default String trimStringToWidth(String text, int width) {
        return trimStringToWidth(text, width, false);
    }
    default void drawSplit(String text, float x, float y, int wrapWidth, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n");
        float currentY = y;
        int fontH = getHeight();
        for (String rawLine : lines) {
            String line = rawLine;
            while (!line.isEmpty()) {
                String trimmed = trimStringToWidth(line, wrapWidth);
                if (trimmed.isEmpty()) {
                    trimmed = line.substring(0, 1);
                }
                draw(trimmed, x, currentY, color, shadow);
                currentY += fontH;
                if (trimmed.length() >= line.length()) {
                    break;
                }
                line = line.substring(trimmed.length()).trim();
            }
        }
    }
}