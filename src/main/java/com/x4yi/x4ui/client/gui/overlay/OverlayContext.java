package com.x4yi.x4ui.client.gui.overlay;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
public class OverlayContext {
    private final GuiScreen screen;
    private final int guiLeft;
    private final int guiTop;
    private final int xSize;
    private final int ySize;
    private final int screenWidth;
    private final int screenHeight;
    private final boolean isContainer;
    public OverlayContext(GuiScreen screen) {
        this.screen = screen;
        this.screenWidth = screen.width;
        this.screenHeight = screen.height;
        if (screen instanceof GuiContainer) {
            GuiContainer container = (GuiContainer) screen;
            this.guiLeft = container.getGuiLeft();
            this.guiTop = container.getGuiTop();
            this.xSize = container.getXSize();
            this.ySize = container.getYSize();
            this.isContainer = true;
        } else {
            this.guiLeft = (screenWidth - 176) / 2;
            this.guiTop = (screenHeight - 166) / 2;
            this.xSize = 176;
            this.ySize = 166;
            this.isContainer = false;
        }
    }
    public GuiScreen getScreen() {
        return screen;
    }
    public int getGuiLeft() {
        return guiLeft;
    }
    public int getGuiTop() {
        return guiTop;
    }
    public int getXSize() {
        return xSize;
    }
    public int getYSize() {
        return ySize;
    }
    public int getScreenWidth() {
        return screenWidth;
    }
    public int getScreenHeight() {
        return screenHeight;
    }
    public boolean isContainer() {
        return isContainer;
    }
}