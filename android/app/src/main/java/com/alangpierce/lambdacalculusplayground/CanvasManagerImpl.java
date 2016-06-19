package com.alangpierce.lambdacalculusplayground;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

public class CanvasManagerImpl implements CanvasManager {
    private final AppState appState;
    private final ExpressionControllerFactoryFactory controllerFactoryFactory;
    private final PointConverter pointConverter;

    private final Set<TopLevelExpressionController> expressionControllers = new HashSet<>();

    public CanvasManagerImpl(
            AppState appState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            PointConverter pointConverter) {
        this.appState = appState;
        this.controllerFactoryFactory = controllerFactoryFactory;
        this.pointConverter = pointConverter;
    }

    @Override
    public void renderInitialData() {
        for (Entry<Integer, ScreenExpression> entry : appState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            renderTopLevelExpression(exprId, screenExpression, false /* placeAbovePalette */);
        }
        for (Entry<String, CanvasPoint> entry : appState.getDefinitionsOnScreen().entrySet()) {
            String defName = entry.getKey();
            CanvasPoint point = entry.getValue();
            UserExpression definition = appState.getAllDefinitions().get(defName);
        }
    }

    @Override
    public TopLevelExpressionController createNewExpression(
            UserExpression expression, ScreenPoint screenPos, boolean placeAbovePalette) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        ScreenExpression screenExpression = ScreenExpression.create(expression, canvasPos);
        int exprId = appState.addScreenExpression(screenExpression);
        return renderTopLevelExpression(exprId, screenExpression, placeAbovePalette);
    }

    @Override
    public TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, ScreenPoint screenPos) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        ScreenExpression screenExpression = ScreenExpression.create(
                expression.getExpression(), canvasPos);
        int exprId = appState.addScreenExpression(screenExpression);
        TopLevelExpressionController controller = controllerFactoryFactory.create(this)
                .wrapInTopLevelController(
                        expression, screenExpression, false /* placeAbovePalette */);
        registerTopLevelExpression(exprId, controller, canvasPos);
        // In some cases (like when pulling a circular reference out of a definition, thus making
        // the definition valid), we can attach an expression to the root with a stale definition
        // state, so just recompute it.
        expression.invalidateDefinitions();
        return controller;
    }

    /**
     * Given a new expression, create a view for it and hook up all necessary callbacks.
     */
    private TopLevelExpressionController renderTopLevelExpression(
            int exprId, ScreenExpression screenExpression, boolean placeAbovePalette) {
        TopLevelExpressionController controller =
                controllerFactoryFactory.create(this).createTopLevelController(
                        screenExpression, placeAbovePalette);
        registerTopLevelExpression(exprId, controller, screenExpression.canvasPos());
        return controller;
    }

    private void registerTopLevelExpression(
            int exprId, TopLevelExpressionController controller, CanvasPoint canvasPos) {
        expressionControllers.add(controller);
        controller.setOnChangeCallback(newController -> {
            if (newController != null) {
                appState.modifyExpression(exprId, newController.getScreenExpression());
            } else {
                appState.deleteExpression(exprId);
                expressionControllers.remove(controller);
            }
        });
        controller.getView().attachToRoot(canvasPos);
    }
}
