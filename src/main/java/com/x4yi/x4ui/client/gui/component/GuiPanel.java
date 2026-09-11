package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.client.gui.component.layout.FlexDirection;
import com.x4yi.x4ui.client.gui.component.layout.FlexLayout;
public class GuiPanel extends GuiComponent {
    public GuiPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.layoutManager = new FlexLayout(FlexDirection.ABSOLUTE, 0, false);
    }
    private FlexLayout getFlexLayout() {
        if (this.layoutManager instanceof FlexLayout) {
            return (FlexLayout) this.layoutManager;
        }
        return null;
    }
    public void setFlexDirection(FlexDirection flexDirection) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setFlexDirection(flexDirection);
        requestLayout();
    }
    public FlexDirection getFlexDirection() {
        FlexLayout flex = getFlexLayout();
        return flex != null ? flex.flexDirection : FlexDirection.ABSOLUTE;
    }
    public void setGap(int gap) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setGap(gap);
        requestLayout();
    }
    public void setFlexWrap(boolean flexWrap) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setFlexWrap(flexWrap);
        requestLayout();
    }
    public void setHugContent(boolean hugContent) {
        FlexLayout flex = getFlexLayout();
        if (flex != null) flex.setHugContent(hugContent);
        requestLayout();
    }
    public GuiPanel withHugContent(boolean hugContent) {
        setHugContent(hugContent);
        return this;
    }
    public boolean isHugContent() {
        FlexLayout flex = getFlexLayout();
        return flex != null && flex.isHugContent();
    }
    protected int backgroundColor = 0x00000000;
    protected int borderColor = 0x00000000;
    protected int borderWidth = 0;
    protected int cornerRadius = 0;
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    public int getBackgroundColor() {
        return this.backgroundColor;
    }
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }
    public int getBorderColor() {
        return this.borderColor;
    }
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }
    public int getBorderWidth() {
        return this.borderWidth;
    }
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
    }
    public int getCornerRadius() {
        return this.cornerRadius;
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if ((backgroundColor & 0xFF000000) != 0) {
            if (cornerRadius > 0) {
                gfx.drawRoundedRect(absX, absY, width, height, cornerRadius, backgroundColor);
            } else {
                gfx.drawRect(absX, absY, width, height, backgroundColor);
            }
        }
        if (borderWidth > 0 && (borderColor & 0xFF000000) != 0) {
            if (cornerRadius > 0) {
                gfx.drawRoundedBorder(absX, absY, width, height, cornerRadius, borderWidth, borderColor);
            } else {
                gfx.drawBorder(absX, absY, width, height, borderWidth, borderColor);
            }
        }
    }
}