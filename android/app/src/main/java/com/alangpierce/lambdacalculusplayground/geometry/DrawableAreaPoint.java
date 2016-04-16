package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DrawableAreaPoint {
    public abstract int getX();
    public abstract int getY();

    public static DrawableAreaPoint create(int x, int y) {
        return new AutoValue_DrawableAreaPoint(x, y);
    }

    public DrawableAreaPoint plus(PointDifference other) {
        return create(getX() + other.getX(), getY() + other.getY());
    }
}
