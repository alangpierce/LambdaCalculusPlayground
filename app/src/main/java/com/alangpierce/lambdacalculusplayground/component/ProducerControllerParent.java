package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface ProducerControllerParent {
    // Create a new top-level expression that the user will start dragging.
    TopLevelExpressionController produceExpression(ScreenPoint screenPos);

    /**
     * Determine if the given expression should be deleted when dragged onto this producer. To allow
     * for silly mistakes, we generally allow the user to place newly-created expressions back as a
     * way of immediately deleting them.
     */
    boolean shouldDeleteExpression(UserExpression expression);
}
