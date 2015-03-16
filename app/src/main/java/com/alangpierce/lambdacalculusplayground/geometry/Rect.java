package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

@AutoValue
public abstract class Rect {
    public abstract Point getTopLeft();
    public abstract Point getBottomRight();

    public static Rect create(Point topLeft, Point bottomRight) {
        return new AutoValue_Rect(topLeft, bottomRight);
    }

    public boolean intersectsWith(Rect other) {
        for (Point corner : getCorners()) {
            if (other.containsPoint(corner)) {
                return true;
            }
        }
        for (Point corner : other.getCorners()) {
            if (containsPoint(corner)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPoint(Point point) {
        return point.getX() >= getTopLeft().getX() &&
                point.getY() >= getTopLeft().getY() &&
                point.getX() <= getBottomRight().getX() &&
                point.getY() <= getBottomRight().getY();
    }

    public List<Point> getCorners() {
        return ImmutableList.of(getTopLeft(), getBottomRight(),
                Point.create(getTopLeft().getX(), getBottomRight().getY()),
                Point.create(getBottomRight().getX(), getTopLeft().getY()));
    }
}
