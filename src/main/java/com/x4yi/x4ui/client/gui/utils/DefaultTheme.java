package com.x4yi.x4ui.client.gui.utils;

import com.x4yi.x4ui.client.gui.component.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.List;

public class DefaultTheme implements ITheme {

    public static final DefaultTheme INSTANCE = new DefaultTheme();

    // Tooltips
    @Override public int getTooltipBackgroundColor() { return 0xF0101010; }
    @Override public int getTooltipBorderColor() { return 0x505000FF; }
    @Override public int getTooltipTextColor() { return 0xFFFFFFFF; }
    @Override public int getTooltipPadding() { return 4; }
    @Override public int getTooltipGap() { return 2; }

    // Core Colors
    @Override public int getPrimaryColor() { return 0xFF3B82F6; }
    @Override public int getSecondaryColor() { return 0xFF1E40AF; }
    @Override public int getBackgroundColor() { return 0xFF1F2937; }
    @Override public int getTextColor() { return 0xFFF3F4F6; }
    @Override public int getDisabledTextColor() { return 0xFF9CA3AF; }

    // Components
    @Override public int getButtonBackgroundColor() { return 0x80000000; }
    @Override public int getButtonHoverColor() { return 0xCC000000; }
    @Override public int getButtonDisabledColor() { return 0x40000000; }

    @Override public int getScrollbarTrackColor() { return 0x20FFFFFF; }
    @Override public int getScrollbarThumbColor() { return 0x60FFFFFF; }
    @Override public int getScrollbarThumbHoverColor() { return 0xA0FFFFFF; }

    @Override public int getSliderTrackColor() { return 0xFF2C2C36; }
    @Override public int getSliderFillColor() { return 0xFF29B6F6; }
    @Override public int getSliderHandleColor() { return 0xFFFFFFFF; }
    @Override public int getSliderHandleHoverColor() { return 0xFFE0E0E6; }

    @Override public int getTextInputBackgroundColor() { return 0xFF000000; }
    @Override public int getTextInputBorderColor() { return 0xFFA0A0A0; }
    @Override public int getTextInputFocusedBorderColor() { return 0xFFFFFFFF; }

    @Override public int getToggleOnColor() { return 0xFF00C853; }
    @Override public int getToggleOffColor() { return 0xFF424248; }

    @Override
    public void drawTooltip(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (textLines == null || textLines.isEmpty()) return;

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int pad = getTooltipPadding();
        int gap = getTooltipGap();

        int maxTextWidth = 0;
        for (String line : textLines) {
            int w = fr.getStringWidth(line);
            if (w > maxTextWidth) {
                maxTextWidth = w;
            }
        }

        int tooltipWidth = maxTextWidth + pad * 2;
        int tooltipHeight = textLines.size() * (fr.FONT_HEIGHT + gap) - gap + pad * 2;

        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + tooltipWidth > screenWidth) {
            x -= 28 + tooltipWidth;
        }
        if (y + tooltipHeight + 6 > screenHeight) {
            y = screenHeight - tooltipHeight - 6;
        }
        if (y < 0) {
            y = 0;
        }

        GuiComponent.drawBorder(x, y, x + tooltipWidth, y + tooltipHeight, getTooltipBorderColor());
        GuiComponent.drawRect(x + 1, y + 1, x + tooltipWidth - 1, y + tooltipHeight - 1, getTooltipBackgroundColor());

        int lineY = y + pad;
        for (String line : textLines) {
            fr.drawStringWithShadow(line, x + pad, lineY, getTooltipTextColor());
            lineY += fr.FONT_HEIGHT + gap;
        }
    }

    @Override
    public void drawTooltipComponent(GuiComponent component, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (component == null) return;
        component.tick(com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime);
        int tooltipWidth = component.getWidth();
        int tooltipHeight = component.getHeight();
        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + tooltipWidth > screenWidth) {
            x -= 28 + tooltipWidth;
        }
        if (y + tooltipHeight + 6 > screenHeight) {
            y = screenHeight - tooltipHeight - 6;
        }
        if (y < 0) {
            y = 0;
        }
        component.setX(x);
        component.setY(y);
        component.render(mouseX, mouseY, Minecraft.getMinecraft().getRenderPartialTicks());
    }
}