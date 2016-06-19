package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.component.ProducerController;
import com.alangpierce.lambdacalculusplayground.component.SlotController;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.alangpierce.lambdacalculusplayground.view.VariableView;

import javax.annotation.Nullable;

public class ExpressionControllerFactoryImpl implements ExpressionControllerFactory {
    private final ExpressionViewRenderer viewRenderer;
    private final DragObservableGenerator dragObservableGenerator;
    private final PointConverter pointConverter;
    private final RelativeLayout canvasRoot;
    private final RelativeLayout abovePaletteRoot;
    private final CanvasManager canvasManager;

    public ExpressionControllerFactoryImpl(
            ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter,
            RelativeLayout canvasRoot, RelativeLayout abovePaletteRoot,
            CanvasManager canvasManager) {
        this.viewRenderer = viewRenderer;
        this.dragObservableGenerator = dragObservableGenerator;
        this.pointConverter = pointConverter;
        this.canvasRoot = canvasRoot;
        this.abovePaletteRoot = abovePaletteRoot;
        this.canvasManager = canvasManager;
    }

    public static ExpressionControllerFactoryFactory createFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, RelativeLayout canvasRoot, RelativeLayout abovePaletteRoot) {
        return topLevelExpressionManager -> new ExpressionControllerFactoryImpl(
                viewRenderer, dragObservableGenerator, pointConverter, canvasRoot, abovePaletteRoot,
                topLevelExpressionManager);
    }

    @Override
    public TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression, boolean placeAbovePalette) {
        ExpressionController exprController = createController(screenExpression.expr());
        return wrapInTopLevelController(
                exprController, screenExpression, placeAbovePalette);
    }

    @Override
    public TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, ScreenExpression screenExpression,
            boolean placeAbovePalette) {
        boolean isExecutable = false;
        TopLevelExpressionView topLevelView = TopLevelExpressionView.render(
                viewRenderer, dragObservableGenerator, pointConverter, canvasRoot, abovePaletteRoot,
                placeAbovePalette, exprController.getView(), isExecutable);
        TopLevelExpressionControllerImpl result =
                new TopLevelExpressionControllerImpl(topLevelView,
                        pointConverter, screenExpression, exprController);
        exprController.setOnChangeCallback(result::handleExprChange);
        return result;
    }

    @Override
    public ExpressionController createController(
            UserExpression userExpression) {
        ExpressionController result = userExpression.visit(
                lambda -> {
                    @Nullable ExpressionController bodyController = null;
                    if (lambda.body() != null) {
                        bodyController = createController(lambda.body());
                    }
                    LambdaView view = LambdaView.render(
                            dragObservableGenerator, viewRenderer, lambda.varName(),
                            bodyController != null ? bodyController.getView() : null);
                    ProducerController producerController = new ProducerController(
                            view.getParameterProducer(),
                            LambdaExpressionController.createProducerParent(
                                    canvasManager, lambda.varName()));
                    SlotController bodySlotController = new SlotController(
                            canvasManager, view.getBodySlot(), bodyController);
                    LambdaExpressionController controller = new LambdaExpressionController(
                            view, producerController, bodySlotController, lambda);
                    bodySlotController.setParent(controller.createSlotParent());
                    if (bodyController != null) {
                        bodyController.setOnChangeCallback(bodySlotController::handleChange);
                    }
                    return controller;
                },
                funcCall -> {
                    ExpressionController funcController = createController(funcCall.func());
                    ExpressionController argController = createController(funcCall.arg());

                    FuncCallView view = FuncCallView.render(dragObservableGenerator, viewRenderer,
                            funcController.getView(), argController.getView());

                    FuncCallExpressionController controller =
                            new FuncCallExpressionController(canvasManager, view,
                                    funcController, argController, funcCall);
                    funcController.setOnChangeCallback(controller::handleFuncChange);
                    argController.setOnChangeCallback(controller::handleArgChange);
                    return controller;
                },
                variable -> {
                    VariableView view = VariableView.render(viewRenderer, variable.varName());
                    return new VariableExpressionController(view, variable);
                }
        );
        return result;
    }

    /**
     * Special routine for building a function, like we do when handling a drop.
     *
     * TODO: Get rid of the code duplication here.
     */
    private FuncCallExpressionController createFuncCall(
            ExpressionController funcController, ExpressionController argController) {
        UserFuncCall funcCall = UserFuncCall.create(
                funcController.getExpression(), argController.getExpression());
        FuncCallView view = FuncCallView.render(dragObservableGenerator, viewRenderer,
                funcController.getView(), argController.getView());
        FuncCallExpressionController result =
                new FuncCallExpressionController(canvasManager, view,
                        funcController, argController, funcCall);
        funcController.setOnChangeCallback(result::handleFuncChange);
        argController.setOnChangeCallback(result::handleArgChange);
        return result;
    }
}
