package com.alangpierce.lambdacalculusplayground.drag;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent.Action;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import autovalue.shaded.com.google.common.common.base.Throwables;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

public class DragObservableGeneratorImpl implements DragObservableGenerator {
    private final TouchObservableManager touchObservableManager;

    public DragObservableGeneratorImpl(TouchObservableManager touchObservableManager) {
        this.touchObservableManager = touchObservableManager;
    }

    @Override
    public Observable<? extends Observable<PointerMotionEvent>> getDragObservable(View view) {
        // Raw stream of Android MotionEvent objects.
        return touchObservableManager.touchObservableForView(view)
                // Raw stream of (pointer id, event) values.
                .compose(this::processMotionEvents)
                // Split by pointer ID (which is guaranteed not to repeat).
                .groupBy(PointerMotionEvent::getPointerId)
                // Offset the points so we get a stream of positions of the top-left corner, not a
                // stream of touch positions.
                .map(new Func1<GroupedObservable<Integer, PointerMotionEvent>, Observable<PointerMotionEvent>>() {
                     @Override
                     public Observable<PointerMotionEvent> call(
                             GroupedObservable<Integer, PointerMotionEvent> dragEvents) {
                         Observable<PointerMotionEvent> cachedEvents = dragEvents.cache();
                         return Observable.combineLatest(
                                 // The view position at the time of the first event determines the
                                 // offset within the view.
                                 cachedEvents.first().map(event ->
                                             event.getScreenPos().minus(Views.getScreenPos(view))),
                                 cachedEvents,
                                 // Shift all points by that offset.
                                 (offset, event) ->
                                         event.withScreenPos(event.getScreenPos().minus(offset)));
                     }
                });
    }

    /**
     * Given a stream of raw motion events for a view, transform it into a nicer set of
     * PointerMotionEvents.
     */
    private Observable<PointerMotionEvent> processMotionEvents(Observable<MotionEvent> observable) {
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
                Point pos = getRawCoords(event, i);

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
    private Point getRawCoords(MotionEvent event, int pointerIndex) {
        try {
            Method getRawAxisValueMethod = MotionEvent.class.getDeclaredMethod(
                    "nativeGetRawAxisValue", long.class, int.class, int.class, int.class);
            Field nativePtrField = MotionEvent.class.getDeclaredField("mNativePtr");
            Field historyCurrentField = MotionEvent.class.getDeclaredField("HISTORY_CURRENT");
            getRawAxisValueMethod.setAccessible(true);
            nativePtrField.setAccessible(true);
            historyCurrentField.setAccessible(true);

            float x = (float) getRawAxisValueMethod.invoke(null, nativePtrField.get(event),
                    MotionEvent.AXIS_X, pointerIndex, historyCurrentField.get(null));
            float y = (float) getRawAxisValueMethod.invoke(null, nativePtrField.get(event),
                    MotionEvent.AXIS_Y, pointerIndex, historyCurrentField.get(null));
            return Point.create((int)x, (int)y);
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException|
                NoSuchFieldException e) {
            throw Throwables.propagate(e);
        }
    }
}
