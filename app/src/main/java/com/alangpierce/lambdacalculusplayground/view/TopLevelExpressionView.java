package com.alangpierce.lambdacalculusplayground.view;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import rx.Observable;

public class TopLevelExpressionView {
    private final DragObservableGenerator dragObservableGenerator;
    private final RelativeLayout rootView;

    private final ExpressionView view;

    public TopLevelExpressionView(
            DragObservableGenerator dragObservableGenerator, RelativeLayout rootView,
            ExpressionView view) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.rootView = rootView;
        this.view = view;
    }

    public static TopLevelExpressionView render(DragObservableGenerator dragObservableGenerator,
            RelativeLayout rootView, ExpressionView exprView) {
        return new TopLevelExpressionView(dragObservableGenerator, rootView, exprView);
    }

    public void attachToRoot() {
        attachToRoot(getScreenPos());
    }

    public Point getScreenPos() {
        return Views.getScreenPos(view.getNativeView());
    }

    public void attachToRoot(Point initialScreenCoords) {
        rootView.addView(view.getNativeView(),
                Views.layoutParamsForScreenPosition(rootView, initialScreenCoords));
    }

    public void setScreenPos(Point screenPos) {
        view.getNativeView().setLayoutParams(
                Views.layoutParamsForScreenPosition(rootView, screenPos));
    }

    public void detachFromRoot() {
        rootView.removeView(view.getNativeView());
    }

    public Observable<? extends Observable<PointerMotionEvent>> getExpressionObservable() {
        return dragObservableGenerator.getDragObservable(view.getNativeView());
    }

    public void startDrag() {
        view.getNativeView().animate()
                .setDuration(100).translationZBy(10).scaleX(1.05f).scaleY(1.05f);
    }

    public void endDrag() {
        view.getNativeView().animate()
                .setDuration(100).translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
    }
}
