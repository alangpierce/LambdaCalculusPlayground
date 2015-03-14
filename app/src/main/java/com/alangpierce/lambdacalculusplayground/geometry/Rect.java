package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Rect {
    public abstract Point getTopLeft();
    public abstract Point getBottomRight();

    public static Rect create(Point topLeft, Point bottomRight) {
        return new AutoValue_Rect(topLeft, bottomRight);
    }
}
