package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface CanvasManager {
    /**
     * This should be called once when setting up the fragment.
     */
    void renderInitialData();

    TopLevelExpressionController createNewExpression(
            UserExpression userExpression, ScreenPoint screenPos, boolean placeAbovePalette);

    /**
     * Take an existing expression and treat it as a new top-level expression, including adding it
     * to the state.
     */
    TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, ScreenPoint screenPos);

    /**
     * Take the given name, create a definition for it if necessary, and place the definition on the
     * canvas at the given point. If it's already on the canvas, just move it.
     *
     * Returns true if the definition was already on the canvas and was moved.
     */
    boolean placeDefinition(String defName, DrawableAreaPoint screenPos);

    void deleteDefinitionIfExists(String defName);

    /**
     * Call when the "automatic numbers" setting changes, which means that we may need to recompute
     * definitions in the definition manager and error markers on the screen.
     */
    void handleAutomaticNumbersChanged();
}
