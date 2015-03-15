package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

public interface TopLevelExpressionCreator {
    TopLevelExpressionController createNewExpression(ScreenExpression screenExpression);
}
