package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
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
import com.alangpierce.lambdacalculusplayground.view.VariableView;

import javax.annotation.Nullable;

public class ExpressionControllerFactoryImpl implements ExpressionControllerFactory {
    private final ExpressionViewRenderer viewRenderer;
    private final DragObservableGenerator dragObservableGenerator;
    private final DragManager dragManager;

    public ExpressionControllerFactoryImpl(
            ExpressionViewRenderer viewRenderer,
            DragObservableGenerator dragObservableGenerator,
            DragManager dragManager) {
        this.viewRenderer = viewRenderer;
        this.dragObservableGenerator = dragObservableGenerator;
        this.dragManager = dragManager;
    }

    @Override
    public TopLevelExpressionController createTopLevelController(
            ScreenExpression screenExpression) {
        ExpressionController exprController = createController(screenExpression.getExpr());
        TopLevelExpressionControllerImpl result =
                new TopLevelExpressionControllerImpl(exprController.getView(), screenExpression);
        exprController.setCallbacks(result::handleExprChange, result::handleExprDetach);
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
                        bodyController != null ? bodyController.getView().getNativeView() : null);
                LambdaExpressionController result = new LambdaExpressionController(
                        ExpressionControllerFactoryImpl.this, view, lambda);
                for (DragSource dragSource : result.getDragSources()) {
                    dragManager.registerDragSource(dragSource);
                }

                if (bodyController != null) {
                    bodyController.setCallbacks(result::handleBodyChange, result::handleBodyDetach);
                }
                return result;
            }
            @Override
            public ExpressionController visit(UserFuncCall funcCall) {
                ExpressionController funcController = createController(funcCall.func);
                ExpressionController argController = createController(funcCall.arg);

                FuncCallView view = FuncCallView.render(dragObservableGenerator, viewRenderer,
                        funcController.getView().getNativeView(),
                        argController.getView().getNativeView());

                FuncCallExpressionController result =
                        new FuncCallExpressionController(view, funcCall);
                funcController.setCallbacks(result::handleFuncChange, result::handleFuncDetach);
                argController.setCallbacks(result::handleArgChange, result::handleArgDetach);
                return result;
            }
            @Override
            public ExpressionController visit(UserVariable variable) {
                VariableView view = VariableView.render(dragObservableGenerator, viewRenderer,
                        variable.varName);
                return new VariableExpressionController(view);
            }
        });
    }
}
