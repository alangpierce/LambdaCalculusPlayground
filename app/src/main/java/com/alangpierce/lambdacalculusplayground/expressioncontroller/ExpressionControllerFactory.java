package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenDefinition;
import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.definitioncontroller.DefinitionController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteLambdaController;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

/**
 * Factory class to create Controller classes for expression objects on the screen. Each controller
 * corresponds directly to a view and manages its interactions with the rest of the world.
 */
public interface ExpressionControllerFactory {
    /**
     * @param placeAbovePalette true if the created expression should be above the palette for its
     *                          initial drag operation.
     */
    TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression, boolean placeAbovePalette);

    /**
     * @param placeAbovePalette true if the expression should be in the special abovePaletteRoot
     *                          where lambda expressions live when they are first created from the
     *                          palette. This is a special case to allow expressions to temporarily
     *                          show up above the palette, and should be false for any other cases.
     */
    TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, ScreenExpression screenExpression,
            boolean placeAbovePalette);

    /**
     * Create a hierarchy of controllers and corresponding views. The resulting controller does not
     * have OnChangeCallback set, so it should be set immediately after creation.
     */
    ExpressionController createController(UserExpression userExpression);

    PaletteLambdaController createPaletteLambdaController(String varName);

    DefinitionController createDefinitionController(ScreenDefinition screenDefinition);

    /**
     * We need to pass the expression manager in dynamically in order to avoid a circular reference.
     * We pass it down to make it easier for individual expressions to construct new instances.
     */
    interface ExpressionControllerFactoryFactory {
        ExpressionControllerFactory create(TopLevelExpressionManager expressionManager);
    }
}
