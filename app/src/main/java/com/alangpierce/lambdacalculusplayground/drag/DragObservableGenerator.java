package com.alangpierce.lambdacalculusplayground.drag;

import rx.Observable;

import android.view.View;

/**
 * For a given view, make an observable for all "drag" strokes originating at that view. Each of
 * those strokes is represented as an observable of points in the stroke.
 */
public interface DragObservableGenerator {
    Observable<? extends Observable<PointerMotionEvent>> getDragObservable(View view);
}
