package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.palette.PaletteLambdaController;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpressionEvaluator;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.alangpierce.lambdacalculusplayground.view.ReferenceView;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.alangpierce.lambdacalculusplayground.view.VariableView;

import javax.annotation.Nullable;

public class ExpressionControllerFactoryImpl implements ExpressionControllerFactory {
    private final ExpressionViewRenderer viewRenderer;
    private final DragObservableGenerator dragObservableGenerator;
    private final PointConverter pointConverter;
    private final DragManager dragManager;
    private final UserExpressionEvaluator userExpressionEvaluator;
    private final RelativeLayout canvasRoot;
    private final RelativeLayout abovePaletteRoot;
    private final TopLevelExpressionManager topLevelExpressionManager;

    public ExpressionControllerFactoryImpl(
            ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter,
            DragManager dragManager, UserExpressionEvaluator userExpressionEvaluator,
            RelativeLayout canvasRoot, RelativeLayout abovePaletteRoot,
            TopLevelExpressionManager topLevelExpressionManager) {
        this.viewRenderer = viewRenderer;
        this.dragObservableGenerator = dragObservableGenerator;
        this.pointConverter = pointConverter;
        this.dragManager = dragManager;
        this.userExpressionEvaluator = userExpressionEvaluator;
        this.canvasRoot = canvasRoot;
        this.abovePaletteRoot = abovePaletteRoot;
        this.topLevelExpressionManager = topLevelExpressionManager;
    }

    public static ExpressionControllerFactoryFactory createFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, DragManager dragManager,
            UserExpressionEvaluator userExpressionEvaluator, RelativeLayout canvasRoot,
            RelativeLayout abovePaletteRoot) {
        return topLevelExpressionManager -> new ExpressionControllerFactoryImpl(
                viewRenderer, dragObservableGenerator, pointConverter, dragManager,
                userExpressionEvaluator, canvasRoot, abovePaletteRoot, topLevelExpressionManager);
    }

    @Override
    public TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression, boolean placeAbovePalette) {
        ExpressionController exprController = createController(screenExpression.getExpr());
        return wrapInTopLevelController(
                exprController, screenExpression, placeAbovePalette);
    }

    @Override
    public TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, ScreenExpression screenExpression,
            boolean placeAbovePalette) {
        boolean isExecutable = userExpressionEvaluator.canStep(screenExpression.getExpr());
        TopLevelExpressionView topLevelView = TopLevelExpressionView.render(
                viewRenderer, dragObservableGenerator, pointConverter, canvasRoot, abovePaletteRoot,
                placeAbovePalette, exprController.getView(), isExecutable);
        TopLevelExpressionControllerImpl result =
                new TopLevelExpressionControllerImpl(topLevelExpressionManager, topLevelView,
                        pointConverter, userExpressionEvaluator, screenExpression, exprController);
        for (DragSource dragSource : result.getDragSources()) {
            dragManager.registerDragSource(dragSource);
        }
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
                    LambdaExpressionController controller = new LambdaExpressionController(
                            topLevelExpressionManager, view, lambda, bodyController);
                    if (bodyController != null) {
                        bodyController.setOnChangeCallback(controller::handleBodyChange);
                    }
                    return controller;
                },
                funcCall -> {
                    ExpressionController funcController = createController(funcCall.func());
                    ExpressionController argController = createController(funcCall.arg());

                    FuncCallView view = FuncCallView.render(dragObservableGenerator, viewRenderer,
                            funcController.getView(), argController.getView());

                    FuncCallExpressionController controller =
                            new FuncCallExpressionController(topLevelExpressionManager, view,
                                    funcController, argController, funcCall);
                    funcController.setOnChangeCallback(controller::handleFuncChange);
                    argController.setOnChangeCallback(controller::handleArgChange);
                    return controller;
                },
                variable -> {
                    VariableView view = VariableView.render(viewRenderer, variable.varName());
                    return new VariableExpressionController(view, variable);
                },
                reference -> {
                    ReferenceView view = ReferenceView.render(viewRenderer, reference.defName());
                    return new ReferenceExpressionController(view, reference);
                }
        );
        for (DragSource dragSource : result.getDragSources()) {
            dragManager.registerDragSource(dragSource);
        }
        for (DropTarget dropTarget : result.getDropTargets(this::createFuncCall)) {
            dragManager.registerDropTarget(dropTarget);
        }
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
                new FuncCallExpressionController(topLevelExpressionManager, view,
                        funcController, argController, funcCall);
        funcController.setOnChangeCallback(result::handleFuncChange);
        argController.setOnChangeCallback(result::handleArgChange);
        for (DragSource dragSource : result.getDragSources()) {
            dragManager.registerDragSource(dragSource);
        }
        for (DropTarget dropTarget : result.getDropTargets(this::createFuncCall)) {
            dragManager.registerDropTarget(dropTarget);
        }
        return result;
    }

    @Override
    public PaletteLambdaController createPaletteLambdaController(String varName) {
        LambdaView view = LambdaView.render(dragObservableGenerator, viewRenderer, varName, null);
        PaletteLambdaController result =
                new PaletteLambdaController(topLevelExpressionManager, view, varName);
        result.registerCallbacks(dragManager);
        return result;
    }
}
