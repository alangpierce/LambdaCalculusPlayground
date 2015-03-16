package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression.UserExpressionVisitor;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViewRenderer;
import com.alangpierce.lambdacalculusplayground.view.FuncCallView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.alangpierce.lambdacalculusplayground.view.VariableView;

import javax.annotation.Nullable;

public class ExpressionControllerFactoryImpl implements ExpressionControllerFactory {
    private final ExpressionViewRenderer viewRenderer;
    private final DragObservableGenerator dragObservableGenerator;
    private final DragManager dragManager;
    private final RelativeLayout rootView;
    private final TopLevelExpressionManager topLevelExpressionManager;

    public ExpressionControllerFactoryImpl(
            ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator,
            DragManager dragManager, RelativeLayout rootView,
            TopLevelExpressionManager topLevelExpressionManager) {
        this.viewRenderer = viewRenderer;
        this.dragObservableGenerator = dragObservableGenerator;
        this.dragManager = dragManager;
        this.rootView = rootView;
        this.topLevelExpressionManager = topLevelExpressionManager;
    }

    public static ExpressionControllerFactoryFactory createFactory(
            ExpressionViewRenderer viewRenderer, DragObservableGenerator dragObservableGenerator,
            DragManager dragManager, RelativeLayout rootView) {
        return topLevelExpressionManager -> new ExpressionControllerFactoryImpl(
                viewRenderer, dragObservableGenerator, dragManager, rootView,
                topLevelExpressionManager);
    }

    @Override
    public TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression) {
        ExpressionController exprController = createController(screenExpression.getExpr());
        return wrapInTopLevelController(exprController, screenExpression);
    }

    @Override
    public TopLevelExpressionController wrapInTopLevelController(
            ExpressionController exprController, ScreenExpression screenExpression) {
        TopLevelExpressionView topLevelView = TopLevelExpressionView.render(
                dragObservableGenerator, rootView, exprController.getView());
        TopLevelExpressionControllerImpl result =
                new TopLevelExpressionControllerImpl(topLevelView, rootView, screenExpression);
        for (DragSource dragSource : result.getDragSources()) {
            dragManager.registerDragSource(dragSource);
        }
        exprController.setOnChangeCallback(result::handleExprChange);
        return result;
    }

    @Override
    public ExpressionController createController(
            UserExpression userExpression) {
        return userExpression.visit(new UserExpressionVisitor<ExpressionController>() {
            @Override
            public ExpressionController visit(UserLambda lambda) {
                @Nullable ExpressionController bodyController = null;
                if (lambda.body != null) {
                    bodyController = createController(lambda.body);
                }
                LambdaView view = LambdaView.render(
                        dragObservableGenerator, viewRenderer, lambda.varName,
                        bodyController != null ? bodyController.getView() : null);
                LambdaExpressionController result = new LambdaExpressionController(
                        topLevelExpressionManager, view, lambda, bodyController);
                for (DragSource dragSource : result.getDragSources()) {
                    dragManager.registerDragSource(dragSource);
                }
                if (bodyController != null) {
                    bodyController.setOnChangeCallback(result::handleBodyChange);
                }
                return result;
            }
            @Override
            public ExpressionController visit(UserFuncCall funcCall) {
                ExpressionController funcController = createController(funcCall.func);
                ExpressionController argController = createController(funcCall.arg);

                FuncCallView view = FuncCallView.render(dragObservableGenerator, viewRenderer,
                        funcController.getView(), argController.getView());

                FuncCallExpressionController result =
                        new FuncCallExpressionController(topLevelExpressionManager, view,
                                funcController, argController, funcCall);
                for (DragSource dragSource : result.getDragSources()) {
                    dragManager.registerDragSource(dragSource);
                }
                funcController.setOnChangeCallback(result::handleFuncChange);
                argController.setOnChangeCallback(result::handleArgChange);
                return result;
            }
            @Override
            public ExpressionController visit(UserVariable variable) {
                VariableView view = VariableView.render(dragObservableGenerator, viewRenderer,
                        variable.varName);
                return new VariableExpressionController(view, variable);
            }
        });
    }
}
