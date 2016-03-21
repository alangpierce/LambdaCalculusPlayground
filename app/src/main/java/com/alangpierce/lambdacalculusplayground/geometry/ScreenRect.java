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
        return getBottomRight().getX() >= other.getTopLeft().getX() &&
                getTopLeft().getX() <= other.getBottomRight().getX() &&
                getBottomRight().getY() >= other.getTopLeft().getY() &&
                getTopLeft().getY() <= other.getBottomRight().getY();
    }

    public ScreenRect rightEdge() {
        return create(
                ScreenPoint.create(getBottomRight().getX(), getTopLeft().getY()), getBottomRight());
    }
}
