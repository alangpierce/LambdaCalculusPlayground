package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;

import rx.Observable;

public interface DragSource {
    /**
     * Return the view that can be used to initiate a drag operation with this drag source.
     */
    Observable<? extends Observable<PointerMotionEvent>> getDragObservable();

    /**
     * Called when a drag event starts. The DragSource should deal with the dragging of the actual
     * expression view, and should return an observable of the events as the drag is happening.
     */
    Observable<DragPacket> handleStartDrag(
            RelativeLayout rootView, Observable<PointerMotionEvent> dragEvents);

    /**
     * Indicate that the drag operation has finished. This should be the point where we modify any
     * relevant backing data values. We want to wait until the drag operation is actually finished
     * so that we can handle "canceling" the operation, e.g. by rotating the screen.
     */
    void handleCommit();
}
