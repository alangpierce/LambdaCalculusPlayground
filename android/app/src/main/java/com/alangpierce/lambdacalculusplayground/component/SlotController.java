package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController.ExpressionControllerProvider;

import javax.annotation.Nullable;

import rx.Subscription;

public class SlotController {
    private final SlotView view;

    private @Nullable Subscription dragActionSubscription;

    private SlotControllerParent parent;
    private @Nullable ExpressionController exprController;

    public SlotController(
            SlotView view,
            @Nullable ExpressionController exprController) {
        this.view = view;
        this.exprController = exprController;
    }

    public void setParent(SlotControllerParent parent) {
        this.parent = parent;
    }

    // Note that the expression produced might be null.
    public void handleChange(ExpressionControllerProvider newControllerProvider) {
        view.detach();
        @Nullable ExpressionController newController = newControllerProvider.produceController();
        parent.updateSlotExpression(newController != null ? newController.getExpression() : null);
        view.attach(newController != null ? newController.getView() : null);
        if (newController != null) {
            newController.setOnChangeCallback(this::handleChange);
        }
        exprController = newController;
        parent.handleChange();
    }
}
