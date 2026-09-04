package com.x4yi.x4ui.client.gui.utils;

import com.x4yi.x4ui.client.gui.component.GuiComponent;
import com.x4yi.x4ui.client.gui.component.GuiLabel;
import com.x4yi.x4ui.client.gui.component.GuiTooltip;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class DefaultTheme implements ITheme {

    public static final DefaultTheme INSTANCE = new DefaultTheme();

    private GuiTooltip cachedTooltipPanel;
    private final List<String> lastTextLines = new ArrayList<>();

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

    @Override
    public void drawTooltip(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (textLines == null || textLines.isEmpty()) return;
        if (cachedTooltipPanel == null) {
            cachedTooltipPanel = new GuiTooltip(0, 0, 10, 10);
            cachedTooltipPanel.setLayer(9999);
        }
        if (!textLines.equals(lastTextLines)) {
            lastTextLines.clear();
            lastTextLines.addAll(textLines);
            cachedTooltipPanel.clearChildren();
            cachedTooltipPanel.setPadding(new com.x4yi.x4ui.client.gui.utils.Insets(getTooltipPadding()));
            cachedTooltipPanel.setGap(getTooltipGap());
            int maxWidth = 0;
            for (String line : textLines) {
                GuiLabel label = new GuiLabel(0, 0, line, getTooltipTextColor());
                cachedTooltipPanel.addChild(label);
                int w = Minecraft.getMinecraft().fontRenderer.getStringWidth(line);
                if (w > maxWidth) maxWidth = w;
            }
            cachedTooltipPanel.setWidth(maxWidth + getTooltipPadding() * 2);
            cachedTooltipPanel.requestLayout();
        }
        cachedTooltipPanel.tick(com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime);
        int tooltipWidth = cachedTooltipPanel.getWidth();
        int tooltipHeight = cachedTooltipPanel.getHeight();
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
        cachedTooltipPanel.setX(x);
        cachedTooltipPanel.setY(y);
        cachedTooltipPanel.render(mouseX, mouseY, Minecraft.getMinecraft().getRenderPartialTicks());
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