package com.x4yi.x4ui.client.gui.overlay;
import java.awt.Point;
public enum ContainerAnchor {
    CONTAINER_LEFT,
    CONTAINER_RIGHT,
    CONTAINER_TOP,
    CONTAINER_BOTTOM,
    WINDOW_TOP_LEFT,
    WINDOW_TOP_RIGHT,
    WINDOW_BOTTOM_LEFT,
    WINDOW_BOTTOM_RIGHT,
    SCREEN_CENTER,
    FULLSCREEN;
    public Point calculate(int guiLeft, int guiTop, int xSize, int ySize,
                          int screenWidth, int screenHeight,
                          int compWidth, int compHeight,
                          int offsetX, int offsetY, int stackOffset) {
        int x;
        int y;
        switch (this) {
            case CONTAINER_LEFT:
                x = guiLeft - compWidth - offsetX;
                y = guiTop + offsetY + stackOffset;
                break;
            case CONTAINER_RIGHT:
                x = guiLeft + xSize + offsetX;
                y = guiTop + offsetY + stackOffset;
                break;
            case CONTAINER_TOP:
                x = guiLeft + offsetX + stackOffset;
                y = guiTop - compHeight - offsetY;
                break;
            case CONTAINER_BOTTOM:
                x = guiLeft + offsetX + stackOffset;
                y = guiTop + ySize + offsetY;
                break;
            case WINDOW_TOP_LEFT:
                x = offsetX;
                y = offsetY + stackOffset;
                break;
            case WINDOW_TOP_RIGHT:
                x = screenWidth - compWidth - offsetX;
                y = offsetY + stackOffset;
                break;
            case WINDOW_BOTTOM_LEFT:
                x = offsetX;
                y = screenHeight - compHeight - offsetY - stackOffset;
                break;
            case WINDOW_BOTTOM_RIGHT:
                x = screenWidth - compWidth - offsetX;
                y = screenHeight - compHeight - offsetY - stackOffset;
                break;
            case SCREEN_CENTER:
            default:
                x = (screenWidth - compWidth) / 2 + offsetX;
                y = (screenHeight - compHeight) / 2 + offsetY + stackOffset;
                break;
        }
        return new Point(x, y);
    }
}