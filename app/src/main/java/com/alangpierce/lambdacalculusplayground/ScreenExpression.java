package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

/**
 * Data object for a top-level expression visible on the screen.
 */
@AutoValue
public abstract class ScreenExpression implements Serializable {
    public abstract UserExpression getExpr();
    public abstract DrawableAreaPoint getCanvasPos();

    public static ScreenExpression create(UserExpression expr, DrawableAreaPoint canvasPos) {
        return new AutoValue_ScreenExpression(expr, canvasPos);
    }
}
