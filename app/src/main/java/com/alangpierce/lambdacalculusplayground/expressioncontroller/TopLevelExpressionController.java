package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import java.util.List;

public interface TopLevelExpressionController {
    TopLevelExpressionView getView();
    void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback);
    List<DragSource> getDragSources();

    interface OnTopLevelChangeCallback {
        void onChange(ScreenExpression newScreenExpression);
    }
}
