package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.CanvasManager;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController.ExpressionControllerProvider;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class SlotController {
    private final CanvasManager canvasManager;
    private final SlotView view;

    private final Subject<Observable<PointerMotionEvent>, Observable<PointerMotionEvent>>
            dragActionSubject = PublishSubject.create();
    private @Nullable Subscription dragActionSubscription;

    private SlotControllerParent parent;
    private @Nullable ExpressionController exprController;

    public SlotController(
            CanvasManager canvasManager,
            SlotView view,
            @Nullable ExpressionController exprController) {
        this.canvasManager = canvasManager;
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
        updateDragActionSubscription();
        if (newController != null) {
            newController.setOnChangeCallback(this::handleChange);
        }
        exprController = newController;
        parent.handleChange();
    }

    public void invalidateDefinitions() {
        if (exprController != null) {
            exprController.invalidateDefinitions();
        }
    }

    private void updateDragActionSubscription() {
        if (dragActionSubscription != null) {
            dragActionSubscription.unsubscribe();
            dragActionSubscription = null;
        }
        @Nullable Observable<? extends Observable<PointerMotionEvent>> observable =
                view.getObservable();
        if (observable != null) {
            dragActionSubscription = observable.subscribe(dragActionSubject);
        }
    }
}
