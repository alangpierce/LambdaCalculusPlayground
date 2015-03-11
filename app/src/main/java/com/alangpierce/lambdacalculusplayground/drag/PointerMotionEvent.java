package com.alangpierce.lambdacalculusplayground.drag;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PointerMotionEvent {
    public abstract int getPointerId();
    public abstract Action getAction();
    public abstract Point getScreenPos();

    public static PointerMotionEvent create(int pointerId, Action action, Point screenPos) {
        return new AutoValue_PointerMotionEvent.Builder()
                .pointerId(pointerId).action(action).screenPos(screenPos).build();
    }

    public Builder toBuilder() {
        return new AutoValue_PointerMotionEvent.Builder(this);
    }

    public PointerMotionEvent withPointerId(int pointerId) {
        return toBuilder().pointerId(pointerId).build();
    }

    public enum Action {
        DOWN,
        MOVE,
        UP
    }

    @AutoValue.Builder
    public interface Builder {
        Builder pointerId(int pointerId);
        Builder action(Action action);
        Builder screenPos(Point screenPos);
        PointerMotionEvent build();
    }
}
