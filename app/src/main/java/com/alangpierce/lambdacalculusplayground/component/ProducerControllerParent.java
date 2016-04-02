package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

public interface ProducerControllerParent {
    TopLevelExpressionController produceExpression(ScreenPoint screenPos);
}
