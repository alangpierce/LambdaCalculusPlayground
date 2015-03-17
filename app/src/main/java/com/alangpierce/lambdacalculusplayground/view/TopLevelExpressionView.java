package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import javax.annotation.Nullable;

import autovalue.shaded.com.google.common.common.base.Preconditions;
import rx.Observable;

public class TopLevelExpressionView {
    private final ExpressionViewRenderer renderer;
    private final DragObservableGenerator dragObservableGenerator;
    private final RelativeLayout rootView;

    private ExpressionView exprView;
    // The execute button, which may or may not be attached to the root view.
    private View executeButton;
    private boolean isExecutable;

    public TopLevelExpressionView(
            ExpressionViewRenderer renderer, DragObservableGenerator dragObservableGenerator,
            RelativeLayout rootView, ExpressionView exprView, View executeButton,
            boolean isExecutable) {
        this.renderer = renderer;
        this.dragObservableGenerator = dragObservableGenerator;
        this.rootView = rootView;
        this.exprView = exprView;
        this.executeButton = executeButton;
        this.isExecutable = isExecutable;
    }

    public static TopLevelExpressionView render(
            ExpressionViewRenderer renderer, DragObservableGenerator dragObservableGenerator,
            RelativeLayout rootView, ExpressionView exprView, boolean isExecutable) {
        View executeButton = renderer.makeExecuteButton();
        return new TopLevelExpressionView(renderer, dragObservableGenerator, rootView, exprView,
                executeButton, isExecutable);
    }

    public Point getScreenPos() {
        return Views.getScreenPos(exprView.getNativeView());
    }

    public void attachToRoot(Point canvasPos) {
        Preconditions.checkState(exprView.getNativeView().getParent() == null,
                "Cannot attach an expression to the root that is already attached.");
        LinearLayout exprNativeView = exprView.getNativeView();
        rootView.addView(exprNativeView, Views.layoutParamsForRelativePos(canvasPos));
        exprView.getNativeView().measure(
                View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Views.updateLayoutParamsToRelativePos(executeButton, canvasPos.plus(
                Point.create(exprNativeView.getMeasuredWidth() - 40,
                        exprNativeView.getMeasuredHeight() - 40)));
        if (isExecutable) {
            rootView.addView(executeButton);
        }
    }

    public void setScreenPos(Point screenPos) {
        exprView.getNativeView().setLayoutParams(
                Views.layoutParamsForScreenPos(rootView, screenPos));

        // TODO: Consolidate with code in attachToRoot.
        LinearLayout exprNativeView = exprView.getNativeView();
        Views.updateLayoutParamsToRelativePos(executeButton,
                screenPos.minus(Views.getScreenPos(rootView)).plus(
                        Point.create(exprNativeView.getMeasuredWidth() - 40,
                                exprNativeView.getMeasuredHeight() - 40)));
    }

    public void setCanvasPos(Point canvasPos) {
        exprView.getNativeView().setLayoutParams(Views.layoutParamsForRelativePos(canvasPos));

        // TODO: Consolidate with code in attachToRoot.
        LinearLayout exprNativeView = exprView.getNativeView();
        Views.updateLayoutParamsToRelativePos(executeButton, canvasPos.plus(
                Point.create(exprNativeView.getMeasuredWidth() - 40,
                        exprNativeView.getMeasuredHeight() - 40)));

    }

    public LinearLayout getNativeView() {
        return exprView.getNativeView();
    }

    public Observable<? extends Observable<PointerMotionEvent>> getExpressionObservable() {
        return dragObservableGenerator.getDragObservable(exprView.getNativeView());
    }

    public void startDrag() {
        exprView.getNativeView().animate()
                .setDuration(100).translationZBy(10).scaleX(1.05f).scaleY(1.05f);
        executeButton.animate().setDuration(150).alpha(0);
        executeButton.setClickable(false);
    }

    public void endDrag() {
        exprView.getNativeView().animate()
                .setDuration(100).translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
        executeButton.animate().setDuration(150).alpha(1);
        executeButton.setClickable(true);
    }

    public void handleExpressionChange(
            ExpressionView newExpression, Point canvasPos, boolean newIsExecutable) {
        rootView.removeView(exprView.getNativeView());
        rootView.addView(newExpression.getNativeView());
        exprView = newExpression;
        isExecutable = newIsExecutable;
        setCanvasPos(canvasPos);
        if (isExecutable) {
            rootView.addView(executeButton);
        } else {
            rootView.removeView(executeButton);
        }
    }

    public void decommission() {
        rootView.removeView(exprView.getNativeView());
        rootView.removeView(executeButton);
    }
}
