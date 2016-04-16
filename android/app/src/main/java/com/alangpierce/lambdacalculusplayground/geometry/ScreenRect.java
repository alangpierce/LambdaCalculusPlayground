package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

/**
 * A rectangle, expressed in screen coordinates.
 */
@AutoValue
public abstract class ScreenRect {
    public abstract ScreenPoint getTopLeft();
    public abstract ScreenPoint getBottomRight();

    public static ScreenRect create(ScreenPoint topLeft, ScreenPoint bottomRight) {
        return new AutoValue_ScreenRect(topLeft, bottomRight);
    }

    public boolean intersectsWith(ScreenRect other) {
        // If a view is just a single point, we say it doesn't intersect with anything. This avoids
        // some bugs where views haven't been measured yet.
        return isNonempty() && other.isNonempty() &&
                getBottomRight().getX() >= other.getTopLeft().getX() &&
                getTopLeft().getX() <= other.getBottomRight().getX() &&
                getBottomRight().getY() >= other.getTopLeft().getY() &&
                getTopLeft().getY() <= other.getBottomRight().getY();
    }

    private boolean isNonempty() {
        return !getBottomRight().equals(getTopLeft());
    }

    public ScreenRect rightEdge() {
        return create(
                ScreenPoint.create(getBottomRight().getX(), getTopLeft().getY()), getBottomRight());
    }
}
