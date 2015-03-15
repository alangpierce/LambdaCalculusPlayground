package com.alangpierce.lambdacalculusplayground.view;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import autovalue.shaded.com.google.common.common.base.Preconditions;
import rx.Observable;

public class TopLevelExpressionView {
    private final DragObservableGenerator dragObservableGenerator;
    private final RelativeLayout rootView;

    private ExpressionView exprView;

    public TopLevelExpressionView(
            DragObservableGenerator dragObservableGenerator, RelativeLayout rootView,
            ExpressionView exprView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.rootView = rootView;
        this.exprView = exprView;
    }

    public static TopLevelExpressionView render(DragObservableGenerator dragObservableGenerator,
            RelativeLayout rootView, ExpressionView exprView) {
        return new TopLevelExpressionView(dragObservableGenerator, rootView, exprView);
    }

    public void attachToRoot() {
        attachToRoot(getScreenPos());
    }

    public Point getScreenPos() {
        return Views.getScreenPos(exprView.getNativeView());
    }

    public void attachToRoot(Point initialScreenCoords) {
        Preconditions.checkState(exprView.getNativeView().getParent() == null,
                "Cannot attach an expression to the root that is already attached.");
        rootView.addView(exprView.getNativeView(),
                Views.layoutParamsForScreenPosition(rootView, initialScreenCoords));
    }

    public void setScreenPos(Point screenPos) {
        exprView.getNativeView().setLayoutParams(
                Views.layoutParamsForScreenPosition(rootView, screenPos));
    }

    public void detachFromRoot() {
        rootView.removeView(exprView.getNativeView());
    }

    public Observable<? extends Observable<PointerMotionEvent>> getExpressionObservable() {
        return dragObservableGenerator.getDragObservable(exprView.getNativeView());
    }

    public void startDrag() {
        exprView.getNativeView().animate()
                .setDuration(100).translationZBy(10).scaleX(1.05f).scaleY(1.05f);
    }

    public void endDrag() {
        exprView.getNativeView().animate()
                .setDuration(100).translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
    }

    public void handleExpressionChange(ExpressionView newExpression) {
        Point screenPos = getScreenPos();
        rootView.removeView(exprView.getNativeView());
        rootView.addView(newExpression.getNativeView());
        exprView = newExpression;
        setScreenPos(screenPos);
    }
}
