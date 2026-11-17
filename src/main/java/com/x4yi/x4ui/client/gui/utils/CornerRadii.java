package com.x4yi.x4ui.client.gui.utils;

import java.util.Objects;

public final class CornerRadii {
    public static final CornerRadii ZERO = new CornerRadii(0, 0, 0, 0);

    public final int topLeft;
    public final int topRight;
    public final int bottomRight;
    public final int bottomLeft;

    public CornerRadii(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.topLeft = Math.max(0, topLeft);
        this.topRight = Math.max(0, topRight);
        this.bottomRight = Math.max(0, bottomRight);
        this.bottomLeft = Math.max(0, bottomLeft);
    }

    public static CornerRadii all(int radius) {
        return radius <= 0 ? ZERO : new CornerRadii(radius, radius, radius, radius);
    }

    public static CornerRadii of(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        if (topLeft <= 0 && topRight <= 0 && bottomRight <= 0 && bottomLeft <= 0) {
            return ZERO;
        }
        return new CornerRadii(topLeft, topRight, bottomRight, bottomLeft);
    }

    public static CornerRadii top(int radius) {
        return radius <= 0 ? ZERO : new CornerRadii(radius, radius, 0, 0);
    }

    public static CornerRadii bottom(int radius) {
        return radius <= 0 ? ZERO : new CornerRadii(0, 0, radius, radius);
    }

    public static CornerRadii left(int radius) {
        return radius <= 0 ? ZERO : new CornerRadii(radius, 0, 0, radius);
    }

    public static CornerRadii right(int radius) {
        return radius <= 0 ? ZERO : new CornerRadii(0, radius, radius, 0);
    }

    public CornerRadii withTopLeft(int r) {
        return of(r, topRight, bottomRight, bottomLeft);
    }

    public CornerRadii withTopRight(int r) {
        return of(topLeft, r, bottomRight, bottomLeft);
    }

    public CornerRadii withBottomRight(int r) {
        return of(topLeft, topRight, r, bottomLeft);
    }

    public CornerRadii withBottomLeft(int r) {
        return of(topLeft, topRight, bottomRight, r);
    }

    public boolean isZero() {
        return topLeft == 0 && topRight == 0 && bottomRight == 0 && bottomLeft == 0;
    }

    public boolean isUniform() {
        return topLeft == topRight && topRight == bottomRight && bottomRight == bottomLeft;
    }

    public CornerRadii clamp(int width, int height) {
        int maxR = Math.max(0, Math.min(width, height) / 2);
        if (topLeft <= maxR && topRight <= maxR && bottomRight <= maxR && bottomLeft <= maxR) {
            return this;
        }
        return of(
                Math.min(topLeft, maxR),
                Math.min(topRight, maxR),
                Math.min(bottomRight, maxR),
                Math.min(bottomLeft, maxR)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CornerRadii)) return false;
        CornerRadii that = (CornerRadii) o;
        return topLeft == that.topLeft
                && topRight == that.topRight
                && bottomRight == that.bottomRight
                && bottomLeft == that.bottomLeft;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLeft, topRight, bottomRight, bottomLeft);
    }
}

