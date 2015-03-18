package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.ExpressionViews;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

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
    public int hitTest(TopLevelExpressionView dragView) {
        if (ExpressionViews.rightEdgeIntersectsWith(targetView, dragView)) {
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
        ExpressionViews.detach(targetView);
        ExpressionController funcController = targetController;
        ExpressionController argController = expressionController.decommission();
        FuncCallExpressionController funcCallController =
                funcCallFactory.createFuncCall(funcController, argController);
        changeCallback.onChange(funcCallController);
    }
}
