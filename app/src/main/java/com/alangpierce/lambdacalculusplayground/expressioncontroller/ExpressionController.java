package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;

import java.util.List;

public interface ExpressionController {
    ExpressionView getView();
    void setOnChangeCallback(OnChangeCallback onChangeCallback);
    List<DragSource> getDragSources();
    List<DropTarget> getDropTargets();

    /**
     * Callback used for expressions to propagate changes in the actual backing UserExpression. For
     * example, dragging an expression out of a larger expression will cause OnChange to propagate
     * up to the top level, where it will be stored in the fragment's state and stored to the bundle
     * if necessary.
     */
    interface OnChangeCallback {
        void onChange(UserExpression newExpression);
    }
}
