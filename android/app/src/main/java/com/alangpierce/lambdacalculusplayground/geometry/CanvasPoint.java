package com.alangpierce.lambdacalculusplayground.geometry;

import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * A point relative to the canvas origin.
 */
@AutoValue
public abstract class CanvasPoint implements Serializable {
    public abstract int getX();
    public abstract int getY();

    public static CanvasPoint create(int x, int y) {
        return new AutoValue_CanvasPoint(x, y);
    }
}
