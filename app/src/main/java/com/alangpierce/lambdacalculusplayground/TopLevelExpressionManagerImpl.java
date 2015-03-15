package com.alangpierce.lambdacalculusplayground;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

import java.util.Map.Entry;

public class TopLevelExpressionManagerImpl
        implements TopLevelExpressionManager, TopLevelExpressionCreator {
    private final TopLevelExpressionState expressionState;
    private final RelativeLayout rootView;
    private final ExpressionControllerFactory controllerFactory;

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState, RelativeLayout rootView,
            ExpressionControllerFactory controllerFactory) {
        this.expressionState = expressionState;
        this.rootView = rootView;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public void renderInitialExpressions() {
        for (Entry<Integer, ScreenExpression> entry : expressionState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            renderTopLevelExpression(exprId, screenExpression);
        }
    }

    @Override
    public TopLevelExpressionController createNewExpression(ScreenExpression screenExpression) {
        int exprId = expressionState.addScreenExpression(screenExpression);
        return renderTopLevelExpression(exprId, screenExpression);
    }

    /**
     * Given a new expression, create a view for it and hook up all necessary callbacks.
     */
    private TopLevelExpressionController renderTopLevelExpression(
            int exprId, ScreenExpression screenExpression) {
        TopLevelExpressionController controller =
                controllerFactory.createTopLevelController(screenExpression);
        controller.setOnChangeCallback(
                // onChange
                (newController) ->
                        expressionState.modifyExpression(
                                exprId, newController.getScreenExpression()));
        controller.getView().attachToRoot(screenExpression.getScreenCoords());
        return controller;
    }
}
