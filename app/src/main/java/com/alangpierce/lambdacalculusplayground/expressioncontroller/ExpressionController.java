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

    /**
     * Called when definitions have changed, which may affect how we display this expression. In
     * particular invalid references should be marked as errors.
     */
    void invalidateDefinitions();

    /*
     * TODO: This has questionable value, currently. Either refactor things so we always register
     * drag sources in a shared way, or just do it in each case.
     */
    List<DragSource> getDragSources();
    List<DropTarget<?>> getDropTargets(FuncCallControllerFactory funcCallFactory);

    /**
     * Callback for expressions to propagate changes, which include changes to the backing model,
     * the display, and the callback hooks.
     *
     * An important nuance is that the argument to this callback is a function returning an
     * expression controller, rather than an expression controller itself. The existing view should
     * be detached before this is called; otherwise the creation of the controller might try to give
     * it a parent when it already has one (e.g. when creating a function call).
     */
    interface OnChangeCallback {
        void onChange(ExpressionControllerProvider controlerProvider);
    }

    interface ExpressionControllerProvider {
        ExpressionController produceController();
    }
}
