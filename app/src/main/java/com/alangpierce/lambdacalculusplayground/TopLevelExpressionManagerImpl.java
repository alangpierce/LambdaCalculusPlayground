package com.alangpierce.lambdacalculusplayground;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import java.util.Map.Entry;

public class TopLevelExpressionManagerImpl implements TopLevelExpressionManager {
    private final TopLevelExpressionState expressionState;
    private final ExpressionControllerFactoryFactory controllerFactoryFactory;
    private final RelativeLayout rootView;

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            RelativeLayout rootView) {
        this.expressionState = expressionState;
        this.controllerFactoryFactory = controllerFactoryFactory;
        this.rootView = rootView;
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
    public TopLevelExpressionController createNewExpression(
            UserExpression expression, Point screenPos) {
        Point canvasPos = screenPos.minus(Views.getScreenPos(rootView));
        ScreenExpression screenExpression = ScreenExpression.create(expression, canvasPos);
        int exprId = expressionState.addScreenExpression(screenExpression);
        return renderTopLevelExpression(exprId, screenExpression);
    }

    @Override
    public TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, Point screenPos) {
        Point canvasPos = screenPos.minus(Views.getScreenPos(rootView));
        ScreenExpression screenExpression = ScreenExpression.create(
                expression.getExpression(), canvasPos);
        int exprId = expressionState.addScreenExpression(screenExpression);
        TopLevelExpressionController controller = controllerFactoryFactory.create(this)
                .wrapInTopLevelController(expression, screenExpression);
        registerTopLevelExpression(exprId, controller, canvasPos);
        return controller;
    }

    /**
     * Given a new expression, create a view for it and hook up all necessary callbacks.
     */
    private TopLevelExpressionController renderTopLevelExpression(
            int exprId, ScreenExpression screenExpression) {
        TopLevelExpressionController controller =
                controllerFactoryFactory.create(this).createTopLevelController(screenExpression);
        registerTopLevelExpression(exprId, controller, screenExpression.getCanvasPos());
        return controller;
    }

    private void registerTopLevelExpression(
            int exprId, TopLevelExpressionController controller, Point canvasPos) {
        controller.setOnChangeCallback(
                // onChange
                (newController) -> {
                    if (newController != null) {
                        expressionState.modifyExpression(
                                exprId, newController.getScreenExpression());
                    } else {
                        expressionState.deleteExpression(exprId);
                    }
                });
        controller.getView().attachToRoot(canvasPos);
    }
}
