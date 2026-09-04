package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.utils.ITheme;
public class GuiTooltip extends GuiPanel {
    public GuiTooltip(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setFlexDirection(FlexDirection.VERTICAL);
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        ITheme theme = getTheme();
        drawBorder(absX, absY, absX + width, absY + height, theme.getTooltipBorderColor());
        drawRect(absX + 1, absY + 1, absX + width - 1, absY + height - 1, theme.getTooltipBackgroundColor());
    }
}