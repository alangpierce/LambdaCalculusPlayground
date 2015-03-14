package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Rect;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DragPacket {
    public abstract Rect getBoundingBox();
    public abstract ExpressionController getExpressionController();

    public static DragPacket create(Rect boundingBox, ExpressionController expressionController) {
        return new AutoValue_DragPacket(boundingBox, expressionController);
    }
}
