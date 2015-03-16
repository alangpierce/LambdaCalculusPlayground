package com.alangpierce.lambdacalculusplayground.drag;

import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import autovalue.shaded.com.google.common.common.collect.Maps;
import rx.Observable;
import rx.Subscriber;

public class TouchObservableManagerImpl implements TouchObservableManager {
    private WeakHashMap<View, Observable<MotionEvent>> cachedEventObservables = new WeakHashMap<>();

    @Override
    public Observable<MotionEvent> touchObservableForView(View view) {
        if (!cachedEventObservables.containsKey(view)) {
            final Set<Subscriber<? super MotionEvent>> subscribers =
                    Collections.newSetFromMap(
                            Maps.<Subscriber<? super MotionEvent>, Boolean>newConcurrentMap());
            cachedEventObservables.put(view, Observable.create(
                    (Observable.OnSubscribe<MotionEvent>) subscribers::add));
            view.setOnTouchListener((v, event) -> {
                // Copy the list of subscribers first, since onNext might end up modifying the list.
                // TODO: Handle unsubscribe and actually unsubscribe when necessary.
                List<Subscriber<? super MotionEvent>> subscriberList =
                        ImmutableList.copyOf(subscribers);
                for (Subscriber<? super MotionEvent> subscriber : subscriberList) {
                    subscriber.onNext(event);
                }
                return true;
            });
        }
        return cachedEventObservables.get(view);
    }
}
