package com.alangpierce.lambdacalculusplayground.drag;

import rx.Observable;

import android.view.MotionEvent;
import android.view.View;

/**
 * Class that provides a stream of MotionEvent instances for any view. Listeners should transform
 * the stream of MotionEvents in whatever way they want.
 *
 * Since Android only allows a single touch listener per view, the implementation keeps a cache of
 * views and listeners. That means that there are a few important things to know when using and
 * hooking up this class:
 * - This class should be the ONLY place we use setOnTouchListener.
 * - This class must be singleton-scoped in the DI configuration.
 */
public interface TouchObservableManager {
    Observable<MotionEvent> touchObservableForView(View view);
}
