package com.alangpierce.lambdacalculusplayground;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface ExpressionViewGenerator {
    View makeTopLevelExpressionView(UserExpression expr);
}
