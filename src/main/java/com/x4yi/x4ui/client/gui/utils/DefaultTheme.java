package com.x4yi.x4ui.client.gui.utils;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import java.util.List;
public class DefaultTheme implements ITheme {
    public static final DefaultTheme INSTANCE = new DefaultTheme();
    @Override public int getTooltipBackgroundColor() { return 0xF0101010; }
    @Override public int getTooltipBorderColor() { return 0x505000FF; }
    @Override public int getTooltipTextColor() { return 0xFFFFFFFF; }
    @Override public int getTooltipPadding() { return 4; }
    @Override public int getTooltipGap() { return 2; }
    @Override public int getPrimaryColor() { return 0xFF3B82F6; }
    @Override public int getSecondaryColor() { return 0xFF1E40AF; }
    @Override public int getBackgroundColor() { return 0xFF1F2937; }
    @Override public int getTextColor() { return 0xFFF3F4F6; }
    @Override public int getDisabledTextColor() { return 0xFF9CA3AF; }
    @Override public int getButtonBackgroundColor() { return 0x80000000; }
    @Override public int getButtonHoverColor() { return 0xCC000000; }
    @Override public int getButtonDisabledColor() { return 0x40000000; }
    @Override public int getButtonPrimaryColor() { return getPrimaryColor(); }
    @Override public int getButtonSecondaryColor() { return getSecondaryColor(); }
    @Override public int getButtonSuccessColor() { return 0xFF10B981; }
    @Override public int getButtonDangerColor() { return 0xFFEF4444; }
    @Override public int getCardBackgroundColor() { return 0xCC181A24; }
    @Override public int getCardBorderColor() { return 0xFF2A2D3D; }
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
    public void drawTooltip(com.x4yi.x4ui.api.client.gui.IGraphics gfx, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (textLines == null || textLines.isEmpty()) return;
        int pad = getTooltipPadding();
        int gap = getTooltipGap();
        int fontHeight = gfx.getFontHeight();
        int maxTextWidth = 0;
        for (String line : textLines) {
            int w = gfx.getStringWidth(line);
            if (w > maxTextWidth) {
                maxTextWidth = w;
            }
        }
        int tooltipWidth = maxTextWidth + pad * 2;
        int tooltipHeight = textLines.size() * (fontHeight + gap) - gap + pad * 2;
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
        gfx.drawBorder(x, y, tooltipWidth, tooltipHeight, 1, getTooltipBorderColor());
        gfx.drawRect(x + 1, y + 1, tooltipWidth - 2, tooltipHeight - 2, getTooltipBackgroundColor());
        int lineY = y + pad;
        for (String line : textLines) {
            gfx.drawString(line, x + pad, lineY, getTooltipTextColor(), true);
            lineY += fontHeight + gap;
        }
    }
    @Override
    public void drawTooltipComponent(com.x4yi.x4ui.api.client.gui.IGraphics gfx, GuiComponent component, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (component == null) return;
        float dt = com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime > 0.0f ? com.x4yi.x4ui.client.gui.base.GuiBaseScreen.deltaTime : 0.016f;
        component.update(dt);
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
        float partialTicks = (Minecraft.getMinecraft() != null) ? Minecraft.getMinecraft().getRenderPartialTicks() : 0.0f;
        component.render(gfx, mouseX, mouseY, partialTicks);
    }
}