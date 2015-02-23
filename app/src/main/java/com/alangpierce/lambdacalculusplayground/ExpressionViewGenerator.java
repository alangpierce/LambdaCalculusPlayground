package com.alangpierce.lambdacalculusplayground;

import android.view.View;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface ExpressionViewGenerator {
    View makeTopLevelExpressionView(UserExpression expr);

    interface ExpressionViewGeneratorFactory {
        ExpressionViewGenerator create(RelativeLayout rootView);
    }
}
