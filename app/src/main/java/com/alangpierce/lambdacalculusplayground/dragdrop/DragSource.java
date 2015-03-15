package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

import rx.Observable;

public interface DragSource {
    /**
     * Return the view that can be used to initiate a drag operation with this drag source.
     */
    Observable<? extends Observable<PointerMotionEvent>> getDragObservable();

    /**
     * Called when a drag event starts. The DragSource should do whatever steps are necessary to
     * produce and register the top-level expression to be dragged, then return it.
     */
    TopLevelExpressionController handleStartDrag();
}
