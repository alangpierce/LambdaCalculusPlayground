package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Rect;
import com.google.auto.value.AutoValue;

/**
 * We want to send a whole ExpressionController and not just a view or UserExpression, since we need
 * to register the necessary callbacks upon receiving the drop.
 */
@AutoValue
public abstract class DragPacket {
    public abstract Rect getBoundingBox();
    public abstract ExpressionController getExpressionController();

    public static DragPacket create(Rect boundingBox, ExpressionController expressionController) {
        return new AutoValue_DragPacket(boundingBox, expressionController);
    }
}
