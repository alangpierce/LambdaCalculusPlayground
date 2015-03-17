package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.palette.PaletteLambdaController;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

/**
 * Factory class to create Controller classes for expression objects on the screen. Each controller
 * corresponds directly to a view and manages its interactions with the rest of the world.
 */
public interface ExpressionControllerFactory {
    TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression);

    TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, ScreenExpression screenExpression);

    /**
     * Create a hierarchy of controllers and corresponding views. The resulting controller does not
     * have OnChangeCallback set, so it should be set immediately after creation.
     */
    ExpressionController createController(UserExpression userExpression);

    PaletteLambdaController createPaletteLambdaController(String varName);

    /**
     * We need to pass the expression manager in dynamically in order to avoid a circular reference.
     * We pass it down to make it easier for individual expressions to construct new instances.
     */
    interface ExpressionControllerFactoryFactory {
        ExpressionControllerFactory create(TopLevelExpressionManager expressionManager);
    }
}
