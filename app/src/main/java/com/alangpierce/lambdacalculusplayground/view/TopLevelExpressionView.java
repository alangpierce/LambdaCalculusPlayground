package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.compat.Compat;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.base.Preconditions;

import rx.Observable;

public class TopLevelExpressionView {
    private final DragObservableGenerator dragObservableGenerator;
    private final PointConverter pointConverter;
    private final RelativeLayout rootView;

    private ExpressionView exprView;
    // This is the original LayoutParams for the view that's attached to the root, or null if the
    // view is not attached to the root. When we detach, we set the layout params back. This is
    // necessary because layout params are both for the margin when in a LinearLayout and for the
    // position when in a RelativeLayout, and the original layout params came from an XML file, so
    // it's not trivial to just restore them.
    private LinearLayout.LayoutParams savedLayoutParams;

    // The execute button, which may or may not be attached to the root view.
    private View executeButton;
    private boolean isExecutable;

    public TopLevelExpressionView(DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, RelativeLayout rootView, ExpressionView exprView,
            View executeButton,  boolean isExecutable) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.pointConverter = pointConverter;
        this.rootView = rootView;
        this.exprView = exprView;
        this.executeButton = executeButton;
        this.isExecutable = isExecutable;
    }

    public static TopLevelExpressionView render(
            ExpressionViewRenderer renderer, DragObservableGenerator dragObservableGenerator,
            PointConverter pointConverter, RelativeLayout rootView, ExpressionView exprView,
            boolean isExecutable) {
        View executeButton = renderer.makeExecuteButton();
        return new TopLevelExpressionView(dragObservableGenerator, pointConverter, rootView,
                exprView, executeButton, isExecutable);
    }

    public ScreenPoint getScreenPos() {
        return Views.getScreenPos(exprView.getNativeView());
    }

    public void attachToRoot(CanvasPoint canvasPos) {
        attach(pointConverter.toDrawableAreaPoint(canvasPos));
    }

    private void attach(DrawableAreaPoint point) {
        LinearLayout nativeView = exprView.getNativeView();
        Preconditions.checkState(!isAttached(), "Tried to attach view, but was already attached.");
        savedLayoutParams = (LinearLayout.LayoutParams) nativeView.getLayoutParams();
        rootView.addView(nativeView, Views.layoutParamsForRelativePos(point));

        invalidateExecuteButton(point);
    }

    public void detach() {
        Preconditions.checkState(isAttached(), "Tried to detach view, but was already detached.");
        LinearLayout nativeView = exprView.getNativeView();
        rootView.removeView(nativeView);
        nativeView.setLayoutParams(savedLayoutParams);
        savedLayoutParams = null;
    }

    private boolean isAttached() {
        boolean result = exprView.getNativeView().getParent() == rootView;
        Preconditions.checkState(
                result == (savedLayoutParams != null),
                "The top-level expression should be attached iff there are saved layout params.");
        return result;

    }

    public void setScreenPos(ScreenPoint screenPos) {
        exprView.getNativeView().setLayoutParams(
                Views.layoutParamsForRelativePos(pointConverter.toDrawableAreaPoint(screenPos)));

        DrawableAreaPoint expressionPos = pointConverter.toDrawableAreaPoint(screenPos);
        recomputeExecuteButtonPosition(expressionPos);
    }

    public void setCanvasPos(DrawableAreaPoint canvasPos) {
        exprView.getNativeView().setLayoutParams(Views.layoutParamsForRelativePos(canvasPos));
        invalidateExecuteButton(canvasPos);
    }

    public LinearLayout getNativeView() {
        return exprView.getNativeView();
    }

    public Observable<? extends Observable<PointerMotionEvent>> getExpressionObservable() {
        return dragObservableGenerator.getDragObservable(exprView.getNativeView());
    }

    public void startDrag() {
        ViewPropertyAnimator animator = exprView.getNativeView().animate()
                .setDuration(100).scaleX(1.05f).scaleY(1.05f);
        Compat.translationZBy(animator, 10);
        executeButton.animate().setDuration(150).alpha(0);
        executeButton.setClickable(false);
    }

    public void endDrag() {
        ViewPropertyAnimator animator = exprView.getNativeView().animate()
                .setDuration(100).scaleX(1.0f).scaleY(1.0f);
        Compat.translationZBy(animator, -10);
        executeButton.animate().setDuration(150).alpha(1);
        executeButton.setClickable(true);
    }

    public void attachNewExpression(
            ExpressionView newExpression, DrawableAreaPoint drawableAreaPoint,
            boolean newIsExecutable) {
        Preconditions.checkState(!isAttached());
        exprView = newExpression;
        isExecutable = newIsExecutable;
        attach(drawableAreaPoint);
    }

    public void decommission() {
        detach();
        rootView.removeView(executeButton);
    }

    public interface OnExecuteListener {
        void execute();
    }

    public void setOnExecuteListener(OnExecuteListener listener) {
        executeButton.setOnClickListener((view) -> listener.execute());
    }

    private void invalidateExecuteButton(DrawableAreaPoint expressionPos) {
        rootView.removeView(executeButton);
        if (isExecutable) {
            recomputeExecuteButtonPosition(expressionPos);
            rootView.addView(executeButton);
        }
    }

    private void recomputeExecuteButtonPosition(DrawableAreaPoint expressionPos) {
        LinearLayout exprNativeView = exprView.getNativeView();
        exprNativeView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        executeButton.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        DrawableAreaPoint executePos = expressionPos.plus(PointDifference.create(
                exprNativeView.getMeasuredWidth() - (executeButton.getMeasuredWidth() / 4),
                exprNativeView.getMeasuredHeight() - (executeButton.getMeasuredHeight() / 4)));
        Views.updateLayoutParamsToRelativePos(executeButton, executePos);
    }
}
