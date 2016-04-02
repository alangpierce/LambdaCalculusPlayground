package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ScreenDefinition {
    public abstract String defName();
    public abstract CanvasPoint canvasPos();

    public static ScreenDefinition create(String defName, CanvasPoint canvasPoint) {
        return new AutoValue_ScreenDefinition(defName, canvasPoint);
    }
}
