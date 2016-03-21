package com.alangpierce.lambdacalculusplayground;

import android.content.Context;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory.ExpressionControllerFactoryFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Points;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
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
    private final RelativeLayout rootView;
    private final Context context;
    private final PanManager panManager;

    public TopLevelExpressionManagerImpl(
            TopLevelExpressionState expressionState,
            ExpressionControllerFactoryFactory controllerFactoryFactory,
            DragManager dragManager, RelativeLayout rootView, PanManager panManager,
            Context context) {
        this.expressionState = expressionState;
        this.controllerFactoryFactory = controllerFactoryFactory;
        this.dragManager = dragManager;
        this.rootView = rootView;
        this.panManager = panManager;
        this.context = context;
    }

    @Override
    public void renderInitialExpressions() {
        for (Entry<Integer, ScreenExpression> entry : expressionState.expressionsById()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            renderTopLevelExpression(exprId, screenExpression);
        }
        renderPalette();
        panManager.init();
    }

    private void renderPalette() {
        PaletteView view = PaletteView.render(context, rootView);
        PaletteController controller = new PaletteController(view);
        controller.registerCallbacks(dragManager);

        int yPos = 50;
        for (String varName : ImmutableList.of("x", "y", "t", "f", "b", "s", "z", "n", "m")) {
            renderPaletteLambda(DrawableAreaPoint.create(1750, yPos), varName);
            yPos += 145;
        }
    }

    private void renderPaletteLambda(DrawableAreaPoint canvasPos, String varName) {
        PaletteLambdaController controller = controllerFactoryFactory.create(this)
                .createPaletteLambdaController(varName);
        rootView.addView(controller.getView().getNativeView(),
                Views.layoutParamsForRelativePos(canvasPos));
    }

    @Override
    public TopLevelExpressionController createNewExpression(
            UserExpression expression, ScreenPoint screenPos) {
        DrawableAreaPoint canvasPos = Points.screenPointToDrawableAreaPoint(screenPos, rootView);
        ScreenExpression screenExpression = ScreenExpression.create(expression, canvasPos);
        int exprId = expressionState.addScreenExpression(screenExpression);
        return renderTopLevelExpression(exprId, screenExpression);
    }

    @Override
    public TopLevelExpressionController sendExpressionToTopLevel(
            ExpressionController expression, ScreenPoint screenPos) {
        DrawableAreaPoint canvasPos = Points.screenPointToDrawableAreaPoint(screenPos, rootView);
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
            int exprId, TopLevelExpressionController controller, DrawableAreaPoint canvasPos) {
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
