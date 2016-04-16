package com.alangpierce.lambdacalculusplayground.component;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import rx.Observable;

public class ProducerView {
    private final DragObservableGenerator dragObservableGenerator;
    private final View nativeView;

    public ProducerView(
            DragObservableGenerator dragObservableGenerator, View nativeView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.nativeView = nativeView;
    }

    public Observable<? extends Observable<PointerMotionEvent>> getObservable() {
        return dragObservableGenerator.getDragObservable(nativeView);
    }

    public ScreenPoint getPos() {
        return Views.getScreenPos(nativeView);
    }

    public boolean intersectsWith(TopLevelExpressionView dragView) {
        return Views.viewsIntersect(nativeView, dragView.getNativeView());
    }
}
