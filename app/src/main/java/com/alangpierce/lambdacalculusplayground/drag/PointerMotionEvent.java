package com.alangpierce.lambdacalculusplayground.drag;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PointerMotionEvent {
    public abstract int getPointerId();
    public abstract Action getAction();
    public abstract Point getScreenPos();

    public static PointerMotionEvent create(int pointerId, Action action, Point screenPos) {
        return new AutoValue_PointerMotionEvent(pointerId, action, screenPos);
    }

    public static enum Action {
        DOWN,
        MOVE,
        UP
    }
}
