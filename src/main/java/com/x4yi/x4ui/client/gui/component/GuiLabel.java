package com.x4yi.x4ui.client.gui.component;
public class GuiLabel extends GuiComponent {
    protected String text;
    protected int color;
    protected boolean centered;
    public GuiLabel(int x, int y, String text, int color) {
        super(x, y, 0, 10);
        this.text = text;
        this.color = color;
        this.centered = false;
        this.width = mc.fontRenderer.getStringWidth(text);
    }
    public GuiLabel setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }
    public void setText(String text) {
        this.text = text;
        this.width = mc.fontRenderer.getStringWidth(text);
    }
    @Override
    protected void renderSelf(int mouseX, int mouseY, float partialTicks) {
        int absX = getAbsoluteX();
        int absY = getAbsoluteY();
        if (centered) {
            drawCenteredString(text, absX + width / 2, absY, color);
        } else {
            drawString(text, absX, absY, color);
        }
    }
}