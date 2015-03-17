package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.FuncCallDropTarget.FuncCallControllerFactory;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;

import java.util.List;

public interface ExpressionController {
    UserExpression getExpression();
    ExpressionView getView();
    void setOnChangeCallback(OnChangeCallback onChangeCallback);

    /**
     * This is useful when making changes that change the onChangeCallback, then calling the
     * original callback before it was set.
     */
    OnChangeCallback getOnChangeCallback();

    /*
     * TODO: This has questionable value, currently. Either refactor things so we always register
     * drag souces in a shared way, or just do it in each case.
     */
    List<DragSource> getDragSources();
    List<DropTarget> getDropTargets(FuncCallControllerFactory funcCallFactory);

    /**
     * Callback for expressions to propagate changes, which include changes to the backing model,
     * the display, and the callback hooks.
     */
    interface OnChangeCallback {
        void onChange(ExpressionController newController);
    }
}
