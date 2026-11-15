package com.x4yi.x4ui.client.gui.utils;

/**
 * Bitmask constants and helpers for per-side rectangular borders.
 */
public final class BorderSides {
    public static final int NONE = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 1 << 1;
    public static final int BOTTOM = 1 << 2;
    public static final int LEFT = 1 << 3;
    public static final int ALL = TOP | RIGHT | BOTTOM | LEFT;

    private BorderSides() {}

    public static boolean has(int mask, int side) {
        return (mask & side) == side;
    }

    public static int without(int mask, int sideToExclude) {
        return mask & ~sideToExclude;
    }

    public static int with(int mask, int sideToInclude) {
        return mask | sideToInclude;
    }
}

