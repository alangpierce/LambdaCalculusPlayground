package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import java.util.List;

public interface TopLevelExpressionController {
    ScreenExpression getScreenExpression();
    TopLevelExpressionView getView();
    void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback);
    void handlePositionChange(Point screenPos);
    List<DragSource> getDragSources();

    interface OnTopLevelChangeCallback {
        void onChange(TopLevelExpressionController newExpressionController);
    }
}
