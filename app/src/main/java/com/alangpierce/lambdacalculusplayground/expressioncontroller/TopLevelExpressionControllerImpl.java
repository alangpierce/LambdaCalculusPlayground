package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public class TopLevelExpressionControllerImpl implements TopLevelExpressionController {
    private final LinearLayout view;

    private ScreenExpression screenExpression;
    private OnTopLevelChangeCallback onChangeCallback;
    private OnTopLevelDetachCallback onDetachCallback;

    public TopLevelExpressionControllerImpl(LinearLayout view, ScreenExpression screenExpression) {
        this.view = view;
        this.screenExpression = screenExpression;
    }

    @Override
    public LinearLayout getView() {
        return view;
    }

    @Override
    public void setCallbacks(OnTopLevelChangeCallback onChangeCallback,
                             OnTopLevelDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }

    public void handleExprChange(UserExpression userExpression) {
        screenExpression = new ScreenExpression(
                userExpression, screenExpression.x, screenExpression.y);
        onChangeCallback.onChange(screenExpression);
    }

    public void handleExprDetach(View view) {
        onDetachCallback.onDetach(view);
    }
}
