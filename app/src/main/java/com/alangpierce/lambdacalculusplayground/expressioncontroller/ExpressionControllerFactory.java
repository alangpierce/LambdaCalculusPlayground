package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

/**
 * Factory class to create Controller classes for expression objects on the screen. Each controller
 * corresponds directly to a view and manages its interactions with the rest of the world.
 */
public interface ExpressionControllerFactory {
    TopLevelExpressionController createTopLevelController(ScreenExpression screenExpression);

    /**
     * Don't actually create any views, but make a top-level controller for this existing view,
     * which should be detached.
     */
    TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, ScreenExpression screenExpression);

    /**
     * Create a hierarchy of controllers and corresponding views. The resulting controller does not
     * have OnChangeCallback set, so it should be set immediately after creation.
     */
    ExpressionController createController(UserExpression userExpression);

    interface ExpressionControllerFactoryFactory {
        ExpressionControllerFactory create(RelativeLayout rootView);
    }
}
