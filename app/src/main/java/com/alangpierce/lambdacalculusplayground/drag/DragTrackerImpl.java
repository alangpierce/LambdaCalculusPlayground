package com.alangpierce.lambdacalculusplayground.drag;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent.Action;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class DragTrackerImpl implements DragTracker {
    private final TouchObservableManager touchObservableManager;

    private Point lastPos;
    private int activePointerId = MotionEvent.INVALID_POINTER_ID;
    private View dragView;

    public DragTrackerImpl(TouchObservableManager touchObservableManager) {
        this.touchObservableManager = touchObservableManager;
    }

    @Override
    public void registerDraggableView(final View view, final StartDragHandler handler) {
        Observable<MotionEvent> motions = touchObservableManager.touchObservableForView(view);
        Observable<PointerMotionEvent> pointerEvents = processMotionEvents(view, motions);
        pointerEvents.subscribe(new Action1<PointerMotionEvent>() {
            @Override
            public void call(PointerMotionEvent event) {
                switch (event.getAction()) {
                    case DOWN: {
                        if (activePointerId != MotionEvent.INVALID_POINTER_ID) {
                            break;
                        }
                        lastPos = event.getScreenPos();
                        activePointerId = event.getPointerId();
                        dragView = handler.onStartDrag();
                        break;
                    }
                    case MOVE:
                        if (activePointerId != event.getPointerId()) {
                            break;
                        }
                        Point pos = event.getScreenPos();
                        moveView(pos.getX() - lastPos.getX(), pos.getY() - lastPos.getY());
                        lastPos = pos;
                        break;
                    case UP:
                        if (event.getPointerId() == activePointerId) {
                            activePointerId = MotionEvent.INVALID_POINTER_ID;
                        }
                        break;
                }
            }
        });
    }

    /**
     * Given a stream of raw motion events for a view, transform it into a nicer set of
     * PointerMotionEvents.
     */
    private Observable<PointerMotionEvent> processMotionEvents(
            final View view, Observable<MotionEvent> observable) {
        return observable.flatMapIterable(
                new Func1<MotionEvent, Iterable<? extends PointerMotionEvent>>() {
            @Override
            public Iterable<PointerMotionEvent> call(MotionEvent event) {
                List<PointerMotionEvent> resultEvents = new ArrayList<>();
                // The action itself can only describe a single pointer, so we see which one it is.
                int actionIndex = MotionEventCompat.getActionIndex(event);
                for (int i = 0; i < MotionEventCompat.getPointerCount(event); i++) {
                    Action action = Action.MOVE;

                    if (i == actionIndex) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_POINTER_DOWN:
                                action = Action.DOWN;
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_POINTER_UP:
                                action = Action.UP;
                                break;
                        }
                    }

                    int pointerId = MotionEventCompat.getPointerId(event, i);
                    Point pos = getRawCoords(view, event, i);

                    resultEvents.add(PointerMotionEvent.create(pointerId, action, pos));
                }
                return resultEvents;
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
     */
    private Point getRawCoords(View v, MotionEvent event, int pointerIndex) {
        final int location[] = { 0, 0 };
        v.getLocationOnScreen(location);
        location[0] += event.getX(pointerIndex);
        location[1] += event.getY(pointerIndex);
        return Point.create(location[0], location[1]);
    }
}
