package com.alangpierce.lambdacalculusplayground.drag;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import rx.functions.Action1;

public class DragTrackerImpl implements DragTracker {
    private final TouchObservableManager touchObservableManager;

    // An array of length 2 (x, y) with the screen coordinates of the last drag position.
    private int[] lastCoords;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private View dragView;

    public DragTrackerImpl(TouchObservableManager touchObservableManager) {
        this.touchObservableManager = touchObservableManager;
    }

    @Override
    public void registerDraggableView(final View view, final StartDragHandler handler) {
        touchObservableManager.touchObservableForView(view).subscribe(new Action1<MotionEvent>() {
            @Override
            public void call(MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (activePointerId != MotionEvent.INVALID_POINTER_ID) {
                            return;
                        }
                        int pointerIndex = MotionEventCompat.getActionIndex(event);
                        lastCoords = getRawCoords(view, event, pointerIndex);
                        activePointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                        dragView = handler.onStartDrag();
                        return;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                            return;
                        }
                        int pointerIndex =
                                MotionEventCompat.findPointerIndex(event, activePointerId);
                        if (pointerIndex == -1) {
                            return;
                        }
                        int[] coords = getRawCoords(view, event, pointerIndex);
                        moveView(coords[0] - lastCoords[0], coords[1] - lastCoords[1]);
                        lastCoords = coords;
                        return;
                    }
                    case MotionEvent.ACTION_UP: {
                        int pointerIndex = MotionEventCompat.getActionIndex(event);
                        int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                        if (pointerId == activePointerId) {
                            activePointerId = MotionEvent.INVALID_POINTER_ID;
                        }
                        return;
                    }
                    default:
                        // Do nothing.
                }
            }
        });
    }

    void moveView(int dx, int dy) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)dragView.getLayoutParams();
        layoutParams.leftMargin += dx;
        layoutParams.topMargin += dy;
        dragView.setLayoutParams(layoutParams);
    }

    /**
     * Get raw screen coordinates for an event. We need to use raw screen coordinates because the
     * drag handle (the origin point of our move operation) may or may not move as we drag.
     *
     * The API doesn't provide this, so we need to compute it more directly:
     * http://stackoverflow.com/questions/6517494/get-motionevent-getrawx-getrawy-of-other-pointers
     *
     * @return An array of length 2 with (x, y) screen coordinates.
     */
    private int[] getRawCoords(View v, MotionEvent event, int pointerIndex) {
        final int location[] = { 0, 0 };
        v.getLocationOnScreen(location);
        location[0] += event.getX(pointerIndex);
        location[1] += event.getY(pointerIndex);
        return location;
    }
}
