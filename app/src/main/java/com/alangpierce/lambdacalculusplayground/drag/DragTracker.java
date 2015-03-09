package com.alangpierce.lambdacalculusplayground.drag;

import android.view.View;

/**
 * State tracker to deal with dragging actions. We need to re-implement some of Android's built-in
 * drag-and-drop functionality because the built-in drag-and-drop only draws transparent drop
 * shadows, and also has a number of features that we don't need.
 */
public interface DragTracker {
    void registerDraggableView(View view, StartDragHandler handler);

    /**
     * Returns a View that should be dragged as the pointer moves.
     */
    interface StartDragHandler {
        View onStartDrag();
    }
}
