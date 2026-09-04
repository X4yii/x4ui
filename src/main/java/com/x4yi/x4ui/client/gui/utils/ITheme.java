package com.x4yi.x4ui.client.gui.utils;

import com.x4yi.x4ui.client.gui.component.GuiComponent;
import java.util.List;

public interface ITheme {
    // Tooltips
    int getTooltipBackgroundColor();
    int getTooltipBorderColor();
    int getTooltipTextColor();
    int getTooltipPadding();
    int getTooltipGap();
    
    // Core Colors
    int getPrimaryColor();
    int getSecondaryColor();
    int getBackgroundColor();
    int getTextColor();
    int getDisabledTextColor();

    // Components
    int getButtonBackgroundColor();
    int getButtonHoverColor();
    int getButtonDisabledColor();

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

    // Tooltip Rendering
    void drawTooltip(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight);
    void drawTooltipComponent(GuiComponent component, int mouseX, int mouseY, int screenWidth, int screenHeight);
}
