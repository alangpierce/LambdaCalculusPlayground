package com.alangpierce.lambdacalculusplayground.drag;

import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Subscriber;

public class TouchObservableManagerImpl implements TouchObservableManager {
    private WeakHashMap<View, Observable<MotionEvent>> cachedEventObservables = new WeakHashMap<>();

    @Override
    public Observable<MotionEvent> touchObservableForView(View view) {
        if (!cachedEventObservables.containsKey(view)) {
            final Map<Subscriber<? super MotionEvent>, Void> subscribers =
                    Collections.synchronizedMap(new WeakHashMap<>());
            cachedEventObservables.put(
                    view, Observable.create(observer -> subscribers.put(observer, null)));
            view.setOnTouchListener((v, event) -> {
                // Copy the list of subscribers first, since onNext might end up modifying the list.
                // TODO: Handle unsubscribe and actually unsubscribe when necessary.
                List<Subscriber<? super MotionEvent>> subscriberList =
                        ImmutableList.copyOf(subscribers.keySet());
                for (Subscriber<? super MotionEvent> subscriber : subscriberList) {
                    subscriber.onNext(event);
                }
                return true;
            });
        }
        return cachedEventObservables.get(view);
    }
}
