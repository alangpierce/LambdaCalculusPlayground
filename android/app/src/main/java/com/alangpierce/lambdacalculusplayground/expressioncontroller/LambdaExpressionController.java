package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;

public class LambdaExpressionController implements ExpressionController {
    private final LambdaView view;

    private UserLambda userLambda;
    private OnChangeCallback onChangeCallback;

    public LambdaExpressionController(LambdaView view, UserLambda userLambda) {
        this.view = view;
        this.userLambda = userLambda;
    }

    @Override
    public void setOnChangeCallback(OnChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public OnChangeCallback getOnChangeCallback() {
        return onChangeCallback;
    }

    @Override
    public UserExpression getExpression() {
        return userLambda;
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

}
