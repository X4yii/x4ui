package com.x4yi.x4ui.client.gui.utils;
import com.x4yi.x4ui.client.gui.component.GuiComponent;
import java.util.List;
public interface ITheme {
    int getTooltipBackgroundColor();
    int getTooltipBorderColor();
    int getTooltipTextColor();
    int getTooltipPadding();
    int getTooltipGap();
    int getPrimaryColor();
    int getSecondaryColor();
    int getBackgroundColor();
    int getTextColor();
    int getDisabledTextColor();
    int getButtonBackgroundColor();
    int getButtonHoverColor();
    int getButtonDisabledColor();
    int getButtonPrimaryColor();
    int getButtonSecondaryColor();
    int getButtonSuccessColor();
    int getButtonDangerColor();
    int getCardBackgroundColor();
    int getCardBorderColor();
    int getScrollbarTrackColor();
    int getScrollbarThumbColor();
    int getScrollbarThumbHoverColor();
    int getSliderTrackColor();
    int getSliderFillColor();
    int getSliderHandleColor();
    int getSliderHandleHoverColor();
    int getTextInputBackgroundColor();
    int getTextInputBorderColor();
    int getTextInputFocusedBorderColor();
    int getToggleOnColor();
    int getToggleOffColor();
    default com.x4yi.x4ui.api.client.font.IFont getFont() {
        return com.x4yi.x4ui.impl.client.font.VanillaFont.INSTANCE;
    }
    default com.x4yi.x4ui.api.client.font.IFont getTitleFont() {
        return getFont();
    }
    default com.x4yi.x4ui.api.client.font.IFont getCodeFont() {
        return getFont();
    }
    void drawTooltip(com.x4yi.x4ui.api.client.gui.IGraphics gfx, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight);
    void drawTooltipComponent(com.x4yi.x4ui.api.client.gui.IGraphics gfx, GuiComponent component, int mouseX, int mouseY, int screenWidth, int screenHeight);
}