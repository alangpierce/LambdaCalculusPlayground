package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

import javax.annotation.Nullable;

@AutoValue
public abstract class ScreenDefinition implements Serializable {
    public abstract String defName();
    public abstract @Nullable UserExpression expr();
    public abstract CanvasPoint canvasPos();

    public static ScreenDefinition create(
            String defName, @Nullable UserExpression expr, CanvasPoint canvasPoint) {
        return new AutoValue_ScreenDefinition(defName, expr, canvasPoint);
    }
}
