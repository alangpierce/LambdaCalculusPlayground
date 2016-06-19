package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface CanvasManager {
    TopLevelExpressionController createNewExpression(
            UserExpression userExpression, ScreenPoint screenPos, boolean placeAbovePalette);

    /**
     * Take an existing expression and treat it as a new top-level expression, including adding it
     * to the state.
     */
    TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, ScreenPoint screenPos);
}
