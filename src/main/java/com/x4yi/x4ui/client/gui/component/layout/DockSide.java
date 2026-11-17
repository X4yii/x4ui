package com.x4yi.x4ui.client.gui.component.layout;

import com.x4yi.x4ui.client.gui.utils.BorderSides;

public enum DockSide {
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public DockSide opposite() {
        switch (this) {
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            case TOP: return BOTTOM;
            case BOTTOM: return TOP;
            default: throw new IllegalStateException("Unexpected side: " + this);
        }
    }

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public boolean isVertical() {
        return this == TOP || this == BOTTOM;
    }

    public int toBorderSide() {
        switch (this) {
            case LEFT: return BorderSides.LEFT;
            case RIGHT: return BorderSides.RIGHT;
            case TOP: return BorderSides.TOP;
            case BOTTOM: return BorderSides.BOTTOM;
            default: return BorderSides.NONE;
        }
    }
}

