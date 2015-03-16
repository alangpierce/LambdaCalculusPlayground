package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

/**
 * Factory class to create Controller classes for expression objects on the screen. Each controller
 * corresponds directly to a view and manages its interactions with the rest of the world.
 */
public interface ExpressionControllerFactory {
    TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression);

    TopLevelExpressionController createTopLevelController(
            UserExpression userExpression, Point screenPos);

    /**
     * Don't actually create any views, but make a top-level controller for this existing view,
     * which should be detached.
     */
    TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, Point screenPos);

    /**
     * Create a hierarchy of controllers and corresponding views. The resulting controller does not
     * have OnChangeCallback set, so it should be set immediately after creation.
     */
    ExpressionController createController(UserExpression userExpression);

    /**
     * We need to pass the expression manager in dynamically in order to avoid a circular reference.
     * We pass it down to make it easier for individual expressions to construct new instances.
     */
    interface ExpressionControllerFactoryFactory {
        ExpressionControllerFactory create(TopLevelExpressionManager expressionManager);
    }
}
