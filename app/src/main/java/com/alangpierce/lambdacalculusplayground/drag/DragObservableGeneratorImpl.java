package com.alangpierce.lambdacalculusplayground.drag;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;

public class DragObservableGeneratorImpl implements DragObservableGenerator {
    private final TouchObservableManager touchObservableManager;

    public DragObservableGeneratorImpl(TouchObservableManager touchObservableManager) {
        this.touchObservableManager = touchObservableManager;
    }

    @Override
    public Observable<? extends Observable<PointerMotionEvent>> getDragObservable(View view) {
        Observable<MotionEvent> motions = touchObservableManager.touchObservableForView(view);
        // Raw stream of (pointer id, event) values.
        return processMotionEvents(view, motions)
                // Split by pointer ID (which is guaranteed not to repeat).
                .groupBy(PointerMotionEvent::getPointerId);
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
