package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;

public class VariableExpressionController implements ExpressionController {
    private final LinearLayout view;

    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;

    public VariableExpressionController(LinearLayout view) {
        this.view = view;
    }

    @Override
    public LinearLayout getView() {
        return view;
    }

    @Override
    public void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }
}
