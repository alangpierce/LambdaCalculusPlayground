package com.alangpierce.lambdacalculusplayground;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class ViewDragger {

    public static void attachToView(View dragHandleView, final View draggedView) {
        dragHandleView.setOnTouchListener(new View.OnTouchListener() {
            private float grabPointX;
            private float grabPointY;
            private int activePointerId = MotionEvent.INVALID_POINTER_ID;

            @Override
            public boolean onTouch(View _, MotionEvent event) {
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
                        float x = MotionEventCompat.getX(event, pointerIndex);
                        float y = MotionEventCompat.getY(event, pointerIndex);
                        float dx = x - grabPointX;
                        float dy = y - grabPointY;
                        RelativeLayout.LayoutParams layoutParams =
                                (RelativeLayout.LayoutParams) draggedView.getLayoutParams();
                        layoutParams.leftMargin += dx;
                        layoutParams.topMargin += dy;
                        draggedView.setLayoutParams(layoutParams);
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
        });
    }
}
