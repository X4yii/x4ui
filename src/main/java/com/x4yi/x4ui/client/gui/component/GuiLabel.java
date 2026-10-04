package com.x4yi.x4ui.client.gui.component;
import com.x4yi.x4ui.api.client.font.IFont;
public class GuiLabel extends GuiComponent {
    protected String text;
    protected int color;
    protected boolean centered;
    public GuiLabel(int x, int y, String text, int color) {
        super(x, y, 0, 10);
        this.text = text;
        this.color = color;
        this.centered = false;
        updateDimensions();
    }
    public GuiLabel setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }
    public void setText(String text) {
        this.text = text;
        updateDimensions();
    }
    public GuiLabel setColor(int color) {
        this.color = color;
        return this;
    }
    public String getText() {
        return text;
    }
    @Override
    public GuiLabel withFont(IFont font) {
        super.withFont(font);
        updateDimensions();
        return this;
    }
    @Override
    public GuiLabel withFont(String fontName) {
        super.withFont(fontName);
        updateDimensions();
        return this;
    }
    @Override
    protected void onAttached(GuiComponent parent) {
        super.onAttached(parent);
        updateDimensions();
    }
    protected void updateDimensions() {
        if (text != null) {
            setWidth(getFont().getWidth(text));
            setHeight(getFont().getHeight());
        } else {
            setWidth(0);
            setHeight(getFont().getHeight());
        }
        requestLayout();
    }
    @Override
    protected void renderSelf(com.x4yi.x4ui.api.client.gui.IGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (centered) {
            gfx.drawStringCentered(text, absX + width / 2.0f, absY, color, true);
        } else {
            gfx.drawString(text, absX, absY, color, true);
        }
    }
}