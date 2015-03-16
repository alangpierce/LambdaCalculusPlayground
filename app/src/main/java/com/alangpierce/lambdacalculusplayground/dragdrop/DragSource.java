package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

import rx.Observable;
import rx.Subscription;

public interface DragSource {
    /**
     * Return the view that can be used to initiate a drag operation with this drag source.
     */
    Observable<? extends Observable<PointerMotionEvent>> getDragObservable();

    /**
     * Called when a drag event starts. The DragSource should do whatever steps are necessary to
     * produce and register the top-level expression to be dragged, then return it. The returned
     * controller should be for an expression that already is attached to the root view and has all
     * callbacks set up appropriately.
     *
     * The observable subscription is provided to allow the handler to indicate that we should no
     * longer care about events from the drag source.
     */
    TopLevelExpressionController handleStartDrag(Subscription subscription);
}
