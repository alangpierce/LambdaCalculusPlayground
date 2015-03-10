package com.alangpierce.lambdacalculusplayground.drag;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Point {
    public abstract int getX();
    public abstract int getY();

    public static Point create(int x, int y) {
        return new AutoValue_Point(x, y);
    }
}
