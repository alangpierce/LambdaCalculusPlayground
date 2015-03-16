package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface TopLevelExpressionManager {
    /**
     * This should be called once when setting up the fragment.
     */
    void renderInitialExpressions();

    TopLevelExpressionController createNewExpression(UserExpression userExpression, Point screenPos);

    /**
     * Take an existing expression and treat it as a new top-level expression, including adding it
     * to the state.
     */
    TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, Point screenPos);
}
