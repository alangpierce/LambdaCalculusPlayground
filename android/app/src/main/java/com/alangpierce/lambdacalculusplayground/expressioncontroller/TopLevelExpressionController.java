package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import java.util.List;

import javax.annotation.Nullable;

public interface TopLevelExpressionController {
    ScreenExpression getScreenExpression();
    TopLevelExpressionView getView();
    void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback);
    void handlePositionChange(ScreenPoint screenPos);
    void onPan();
    void invalidateDefinitions();

    /**
     * Get rid of this top-level expression and return the underlying expression controller. This is
     * used when dropping top-level expressions into other expressions.
     */
    ExpressionController decommission();

    interface OnTopLevelChangeCallback {
        void onChange(@Nullable TopLevelExpressionController newExpressionController);
    }
}
