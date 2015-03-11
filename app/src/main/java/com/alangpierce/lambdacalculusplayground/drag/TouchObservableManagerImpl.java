package com.alangpierce.lambdacalculusplayground.drag;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

public class TouchObservableManagerImpl implements TouchObservableManager {
    private WeakHashMap<View, Observable<MotionEvent>> cachedEventObservables = new WeakHashMap<>();

    @Override
    public Observable<MotionEvent> touchObservableForView(View view) {
        if (!cachedEventObservables.containsKey(view)) {
            final Map<Subscriber<? super MotionEvent>, Void> subscribers =
                    Collections.synchronizedMap(new WeakHashMap<>());
            cachedEventObservables.put(view, Observable.create(observer -> subscribers.put(observer, null)));
            view.setOnTouchListener((v, event) -> {
                for (Subscriber<? super MotionEvent> subscriber : subscribers.keySet()) {
                    subscriber.onNext(event);
                }
                return true;
            });
        }
        return cachedEventObservables.get(view);
    }
}
