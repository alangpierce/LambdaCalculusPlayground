package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;

public class TopLevelExpressionControllerImpl implements TopLevelExpressionController {
    private final ExpressionView view;

    private ScreenExpression screenExpression;
    private OnTopLevelChangeCallback onChangeCallback;
    private OnTopLevelDetachCallback onDetachCallback;

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
