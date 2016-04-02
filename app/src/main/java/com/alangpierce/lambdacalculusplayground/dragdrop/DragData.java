package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

/**
 * Visitor-style interface for anything draggable.
 */
public interface DragData {
    <T> T visit(
            Visitor<TopLevelExpressionController, T> expressionVisitor);

    interface Visitor<V, R> {
        R accept(V value);
    }

    // Tell the drag object that dragging is starting.
    void startDrag();

    // Set the position of the drag element during the drag operation.
    void setScreenPos(ScreenPoint screenPos);

    // Finish the drag by landing on a drop target.
    void endDrag();
    // Finish the drag by landing on the canvas at the given screen position, rather than a drop
    // target.
    void handlePositionChange(ScreenPoint screenPos);
}
