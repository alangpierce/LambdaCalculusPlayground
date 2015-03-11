package com.alangpierce.lambdacalculusplayground.drag;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import autovalue.shaded.com.google.common.common.base.Throwables;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

public class DragTrackerImpl implements DragTracker {
    private final TouchObservableManager touchObservableManager;

    private Point lastPos;
    private boolean isActive = false;
    private View dragView;

    public DragTrackerImpl(TouchObservableManager touchObservableManager) {
        this.touchObservableManager = touchObservableManager;
    }

    @Override
    public void registerDraggableView(final View view, final StartDragHandler handler) {
        Observable<MotionEvent> motions = touchObservableManager.touchObservableForView(view);
        Observable<? extends Observable<PointerMotionEvent>> dragEvents =
                // Raw stream of (pointer id, event) values.
                processMotionEvents(view, motions)
                // Split by pointer ID (which is guaranteed not to repeat).
                .groupBy(PointerMotionEvent::getPointerId);

        dragEvents.subscribe(dragObservable -> {
            // Ignore drag events that come in while one is in progress.
            if (isActive) {
                dragObservable.take(0);
                return;
            }
            isActive = true;
            dragObservable.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        lastPos = event.getScreenPos();
                        dragView = handler.onStartDrag();
                        break;
                    }
                    case MOVE: {
                        Point pos = event.getScreenPos();
                        moveView(pos.getX() - lastPos.getX(), pos.getY() - lastPos.getY());
                        lastPos = pos;
                        break;
                    }
                    case UP:
                        isActive = false;
                        break;
                }
            });
        });
    }

    /**
     * Given a stream of raw motion events for a view, transform it into a nicer set of
     * PointerMotionEvents.
     */
    private Observable<PointerMotionEvent> processMotionEvents(
            final View view, Observable<MotionEvent> observable) {
        /*
         * Create nice-looking events that still have their "native" pointer ID: the one assigned by
         * Android, which might repeat.
         */
        Observable<PointerMotionEvent> nativePointerEvents = observable.flatMapIterable(event -> {
            List<PointerMotionEvent> resultEvents = new ArrayList<>();
            // The action itself can only describe a single pointer, so we see which one it is.
            int actionIndex = MotionEventCompat.getActionIndex(event);
            for (int i = 0; i < MotionEventCompat.getPointerCount(event); i++) {
                Action action = Action.MOVE;
                int pointerId = MotionEventCompat.getPointerId(event, i);
                Point pos = getRawCoords(view, event, i);

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

                resultEvents.add(PointerMotionEvent.create(pointerId, action, pos));
            }
            return resultEvents;
        });

        /*
         * Use a hash table to turn repeated pointer IDs into unique IDs. Note that it may make
         * sense to later
         */
        final Map<Integer, Integer> idByNativeId = Collections.synchronizedMap(new HashMap<>());
        final AtomicInteger nextId = new AtomicInteger();
        return nativePointerEvents.map(event -> {
            if (!idByNativeId.containsKey(event.getPointerId())) {
                idByNativeId.put(event.getPointerId(), nextId.incrementAndGet());
            }
            PointerMotionEvent result = event.withPointerId(idByNativeId.get(event.getPointerId()));
            if (event.getAction() == Action.UP) {
                idByNativeId.remove(event.getPointerId());
            }
            return result;
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
