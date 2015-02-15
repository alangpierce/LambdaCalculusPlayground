package com.alangpierce.lambdacalculusplayground;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

public class ViewDragger implements View.OnTouchListener {
    private final OnDragListener dragListener;

    private float grabPointX;
    private float grabPointY;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;

    public ViewDragger(OnDragListener dragListener) {
        this.dragListener = dragListener;
    }

    // TODO(alan): Think about rounding error here from the fact that we're only presenting ints but
    // the underlying stream is floats.
    public static interface OnDragListener {
        public void onDrag(int dx, int dy);
    }

    public static void attachToView(View dragHandleView, OnDragListener dragListener) {
        dragHandleView.setOnTouchListener(new ViewDragger(dragListener));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int pointerIndex = MotionEventCompat.getActionIndex(event);
                grabPointX = event.getX(pointerIndex);
                grabPointY = event.getY(pointerIndex);
                activePointerId = MotionEventCompat.getPointerId(event, pointerIndex);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int pointerIndex =
                        MotionEventCompat.findPointerIndex(event, activePointerId);
                if (pointerIndex == -1) {
                    return true;
                }
                // We assume that the drag handle itself is being moved.
                float x = MotionEventCompat.getX(event, pointerIndex);
                float y = MotionEventCompat.getY(event, pointerIndex);
                float dx = x - grabPointX;
                float dy = y - grabPointY;
                dragListener.onDrag((int)dx, (int)dy);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                return true;
            }
            default:
                return true;
        }
    }
}
