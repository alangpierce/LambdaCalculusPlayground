package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;

public class TopLevelExpressionControllerImpl implements TopLevelExpressionController {
    private final ExpressionView view;

    private ScreenExpression screenExpression;
    private OnTopLevelChangeCallback onChangeCallback;

    public TopLevelExpressionControllerImpl(ExpressionView view,
            ScreenExpression screenExpression) {
        this.view = view;
        this.screenExpression = screenExpression;
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    @Override
    public void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    public void handleExprChange(UserExpression userExpression) {
        screenExpression =
                ScreenExpression.create(userExpression, screenExpression.getScreenCoords());
        onChangeCallback.onChange(screenExpression);
    }
}
