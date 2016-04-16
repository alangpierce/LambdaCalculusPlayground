package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;

import rx.Observable;

public interface DragSource {
    /**
     * Return the view that can be used to initiate a drag operation with this drag source.
     */
    Observable<? extends Observable<PointerMotionEvent>> getDragObservable();

    /**
     * Called when a drag event starts. The DragSource should do whatever steps are necessary to
     * produce and register the data to be dragged (maybe a top-level expression, maybe something
     * else(, then return it. The returned object should be for an expression that already is
     * attached to the root view and has all callbacks set up appropriately.
     */
    DragData handleStartDrag();
}
