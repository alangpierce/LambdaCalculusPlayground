package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;

public interface TopLevelExpressionController {
    ExpressionView getView();
    void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback);

    interface OnTopLevelChangeCallback {
        void onChange(ScreenExpression newScreenExpression);
    }
}
