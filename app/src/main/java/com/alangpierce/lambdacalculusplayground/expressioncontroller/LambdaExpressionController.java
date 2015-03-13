package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public class LambdaExpressionController implements ExpressionController {
    private final LinearLayout view;

    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;

    public LambdaExpressionController(LinearLayout view) {
        this.view = view;
    }

    @Override
    public void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }

    @Override
    public LinearLayout getView() {
        return view;
    }

    public void handleBodyChange(UserExpression newExpression) {

    }

    public void handleBodyDetach() {

    }
}
