package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.utils.ITheme;
public class GuiTooltip extends GuiPanel {
    public GuiTooltip(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.setFlexDirection(FlexDirection.VERTICAL);
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        ITheme theme = getTheme();
        gfx.drawBorder(absX, absY, width, height, 1, theme.getTooltipBorderColor());
        gfx.drawRect(absX + 1, absY + 1, width - 2, height - 2, theme.getTooltipBackgroundColor());
    }
}