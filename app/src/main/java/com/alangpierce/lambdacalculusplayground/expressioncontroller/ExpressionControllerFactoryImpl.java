package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression.UserExpressionVisitor;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

import javax.annotation.Nullable;

public class ExpressionControllerFactoryImpl implements ExpressionControllerFactory {
    private final RelativeLayout rootView;
    private final ExpressionViewRenderer viewRenderer;

    public ExpressionControllerFactoryImpl(
            RelativeLayout rootView, ExpressionViewRenderer viewRenderer) {
        this.rootView = rootView;
        this.viewRenderer = viewRenderer;
    }

    public static ExpressionControllerFactoryFactory createFactory(
            final ExpressionViewRenderer viewRenderer) {
        return (rootView) -> new ExpressionControllerFactoryImpl(rootView, viewRenderer);
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
                LinearLayout view = viewRenderer.makeLambdaView(
                        lambda.varName, bodyController != null ? bodyController.getView() : null);
                LambdaExpressionController result = new LambdaExpressionController(view);
                if (bodyController != null) {
                    bodyController.setCallbacks(result::handleBodyChange, result::handleBodyDetach);
                }
                return result;
            }
            @Override
            public ExpressionController visit(UserFuncCall funcCall) {
                ExpressionController funcController = createController(funcCall.func);
                ExpressionController argController = createController(funcCall.arg);

                LinearLayout view = viewRenderer.makeFuncCallView(
                        funcController.getView(), argController.getView());

                FuncCallExpressionController result = new FuncCallExpressionController(view);
                funcController.setCallbacks(result::handleFuncChange, result::handleFuncDetach);
                argController.setCallbacks(result::handleArgChange, result::handleArgDetach);
                return result;
            }
            @Override
            public ExpressionController visit(UserVariable variable) {
                LinearLayout view = viewRenderer.makeVariableView(variable.varName);
                return new VariableExpressionController(view);
            }
        });
    }
}
