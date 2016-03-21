package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

/**
 * A point representing a position on the screen.
 */
@AutoValue
public abstract class ScreenPoint {
    public abstract int getX();
    public abstract int getY();

    public static ScreenPoint create(int x, int y) {
        return new AutoValue_ScreenPoint(x, y);
    }

    public ScreenPoint plus(PointDifference other) {
        return create(getX() + other.getX(), getY() + other.getY());
    }

    public PointDifference minus(ScreenPoint other) {
        return PointDifference.create(getX() - other.getX(), getY() - other.getY());
    }

    public ScreenPoint minus(PointDifference other) {
        return ScreenPoint.create(getX() - other.getX(), getY() - other.getY());
    }
}

