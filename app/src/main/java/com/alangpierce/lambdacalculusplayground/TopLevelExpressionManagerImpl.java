package com.alangpierce.lambdacalculusplayground;

import android.support.v4.widget.DrawerLayout;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.palette.PaletteController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteLambdaController;
import com.alangpierce.lambdacalculusplayground.palette.PaletteView;
import com.alangpierce.lambdacalculusplayground.pan.PanManager;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.common.collect.ImmutableList;

import java.util.Map.Entry;

public class TopLevelExpressionManagerImpl implements TopLevelExpressionManager {
    private final TopLevelExpressionState expressionState;
    private final ExpressionControllerFactoryFactory controllerFactoryFactory;
    private final DragManager dragManager;
    private final PointConverter pointConverter;
    private final DrawerLayout drawerRoot;
    private final PanManager panManager;

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            DragManager dragManager, PointConverter pointConverter, DrawerLayout drawerRoot,
            PanManager panManager) {
        this.expressionState = expressionState;
        this.controllerFactoryFactory = controllerFactoryFactory;
        this.dragManager = dragManager;
        this.pointConverter = pointConverter;
        this.drawerRoot = drawerRoot;
        this.panManager = panManager;
    }

    @Override
    public void renderInitialExpressions() {
        // Note that we need to initialize the pan manager first so that the rest of the code will
        // correctly get the pan offset.
        panManager.init(expressionState.getPanOffset());
        panManager.registerPanListener(
                () -> expressionState.setPanOffset(panManager.getPanOffset()));

        for (Entry<Integer, ScreenExpression> entry : expressionState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            renderTopLevelExpression(exprId, screenExpression);
        }
        renderPalette();
    }

    private void renderPalette() {
        PaletteView view = PaletteView.render(drawerRoot);
        PaletteController controller = new PaletteController(view);
        controller.registerCallbacks(dragManager);

        for (String varName : ImmutableList.of("x", "y", "t", "f", "b", "s", "z", "n", "m")) {
            PaletteLambdaController lambdaController =
                    controllerFactoryFactory.create(this).createPaletteLambdaController(varName);
            view.addChild(lambdaController.getView());
        }
    }

    @Override
    public TopLevelExpressionController createNewExpression(
            UserExpression expression, ScreenPoint screenPos) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
        ScreenExpression screenExpression = ScreenExpression.create(expression, canvasPos);
        int exprId = expressionState.addScreenExpression(screenExpression);
        return renderTopLevelExpression(exprId, screenExpression);
    }

    @Override
    public TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, ScreenPoint screenPos) {
        CanvasPoint canvasPos = pointConverter.toCanvasPoint(screenPos);
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
            int exprId, TopLevelExpressionController controller, CanvasPoint canvasPos) {
        panManager.registerPanListener(controller);
        controller.setOnChangeCallback(
                // onChange
                (newController) -> {
                    if (newController != null) {
                        expressionState.modifyExpression(
                                exprId, newController.getScreenExpression());
                    } else {
                        expressionState.deleteExpression(exprId);
                        panManager.unregisterPanListener(controller);
                    }
                });
        controller.getView().attachToRoot(canvasPos);
    }
}
