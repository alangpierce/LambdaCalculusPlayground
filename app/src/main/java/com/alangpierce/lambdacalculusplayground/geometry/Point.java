package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class Point implements Serializable {
    public abstract int getX();
    public abstract int getY();

    public static Point create(int x, int y) {
        return new AutoValue_Point(x, y);
    }

    public Point plus(Point other) {
        return create(getX() + other.getX(), getY() + other.getY());
    }

    public Point minus(Point other) {
        return create(getX() - other.getX(), getY() - other.getY());
    }
}
