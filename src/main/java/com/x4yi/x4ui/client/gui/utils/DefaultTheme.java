package com.x4yi.x4ui.client.gui.utils;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import java.util.List;
public class DefaultTheme implements ITheme {
    public static final DefaultTheme INSTANCE = new DefaultTheme();
    @Override public int getTooltipBackgroundColor() { return 0xFF101216; }
    @Override public int getTooltipBorderColor() { return 0xFF2E3442; }
    @Override public int getTooltipTextColor() { return 0xFFF3F4F6; }
    @Override public int getTooltipPadding() { return 5; }
    @Override public int getTooltipGap() { return 2; }
    @Override public int getPrimaryColor() { return 0xFF3B82F6; }
    @Override public int getSecondaryColor() { return 0xFF2563EB; }
    @Override public int getBackgroundColor() { return 0xFF14161B; }
    @Override public int getTextColor() { return 0xFFE5E7EB; }
    @Override public int getDisabledTextColor() { return 0xFF6B7280; }
    @Override public int getButtonBackgroundColor() { return 0xFF232732; }
    @Override public int getButtonHoverColor() { return 0xFF2E3443; }
    @Override public int getButtonDisabledColor() { return 0xFF1A1D24; }
    @Override public int getButtonPrimaryColor() { return getPrimaryColor(); }
    @Override public int getButtonSecondaryColor() { return getSecondaryColor(); }
    @Override public int getButtonSuccessColor() { return 0xFF10B981; }
    @Override public int getButtonDangerColor() { return 0xFFEF4444; }
    @Override public int getCardBackgroundColor() { return 0xFF1B1E26; }
    @Override public int getCardBorderColor() { return 0xFF2A2F3D; }
    @Override public int getScrollbarTrackColor() { return 0xFF161920; }
    @Override public int getScrollbarThumbColor() { return 0xFF383F50; }
    @Override public int getScrollbarThumbHoverColor() { return 0xFF4F586E; }
    @Override public int getSliderTrackColor() { return 0xFF262B36; }
    @Override public int getSliderFillColor() { return 0xFF3B82F6; }
    @Override public int getSliderHandleColor() { return 0xFFF3F4F6; }
    @Override public int getSliderHandleHoverColor() { return 0xFFFFFFFF; }
    @Override public int getTextInputBackgroundColor() { return 0xFF101216; }
    @Override public int getTextInputBorderColor() { return 0xFF2E3442; }
    @Override public int getTextInputFocusedBorderColor() { return 0xFF3B82F6; }
    @Override public int getToggleOnColor() { return 0xFF10B981; }
    @Override public int getToggleOffColor() { return 0xFF374151; }
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