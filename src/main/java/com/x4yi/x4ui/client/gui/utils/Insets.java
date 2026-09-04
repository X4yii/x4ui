package com.x4yi.x4ui.client.gui.utils;

public class Insets {
    public static final Insets ZERO = new Insets(0);

    public final int top;
    public final int right;
    public final int bottom;
    public final int left;

    public Insets(int all) {
        this.top = all;
        this.right = all;
        this.bottom = all;
        this.left = all;
    }

    public Insets(int vertical, int horizontal) {
        this.top = vertical;
        this.bottom = vertical;
        this.right = horizontal;
        this.left = horizontal;
    }

    public Insets(int top, int right, int bottom, int left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }
}
