package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Rect {
    public abstract Point getTopLeft();
    public abstract Point getBottomRight();

    public static Rect create(Point topLeft, Point bottomRight) {
        return new AutoValue_Rect(topLeft, bottomRight);
    }

    public boolean intersectsWith(Rect other) {
        return getBottomRight().getX() >= other.getTopLeft().getX() &&
                getTopLeft().getX() <= other.getBottomRight().getX() &&
                getBottomRight().getY() >= other.getTopLeft().getY() &&
                getTopLeft().getY() <= other.getBottomRight().getY();
    }

    public Rect rightEdge() {
        return create(Point.create(getBottomRight().getX(), getTopLeft().getY()), getBottomRight());
    }
}
