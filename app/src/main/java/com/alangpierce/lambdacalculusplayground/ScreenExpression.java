package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * Data object for a top-level expression visible on the screen.
 */
@AutoValue
public abstract class ScreenExpression implements Serializable {
    public abstract UserExpression getExpr();
    public abstract CanvasPoint getCanvasPos();

    public static ScreenExpression create(UserExpression expr, CanvasPoint canvasPos) {
        return new AutoValue_ScreenExpression(expr, canvasPos);
    }
}
