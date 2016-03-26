package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViews;

public class FuncCallDropTarget implements DropTarget {
    private final ExpressionController targetController;
    private final ExpressionView targetView;
    private final FuncCallControllerFactory funcCallFactory;

    public FuncCallDropTarget(ExpressionController targetController, ExpressionView targetView,
                              FuncCallControllerFactory funcCallFactory) {
        this.targetController = targetController;
        this.targetView = targetView;
        this.funcCallFactory = funcCallFactory;
    }

    public interface FuncCallControllerFactory {
        FuncCallExpressionController createFuncCall(
                ExpressionController funcController, ExpressionController argController);
    }

    @Override
    public int hitTest(TopLevelExpressionController dragController) {
        if (ExpressionViews.rightEdgeIntersectsWith(targetView, dragController.getView())) {
            return Views.viewDepth(targetView.getNativeView());
        } else {
            return DropTarget.NOT_HIT;
        }
    }
    @Override
    public void handleEnter(TopLevelExpressionController expressionController) {
        ExpressionViews.handleDragEnter(targetView);
    }
    @Override
    public void handleExit() {
        ExpressionViews.handleDragExit(targetView);
    }
    @Override
    public void handleDrop(TopLevelExpressionController expressionController) {
        // Setting up the top-level expression modifies our callback so save it first.
        ExpressionController.OnChangeCallback changeCallback =
                targetController.getOnChangeCallback();
        ExpressionController funcController = targetController;
        ExpressionController argController = expressionController.decommission();
        // Delay the creation of the new function call view, since it relies on the existing
        // function view being detached from the view hierarchy.
        changeCallback.onChange(
                () -> funcCallFactory.createFuncCall(funcController, argController));
    }
}
