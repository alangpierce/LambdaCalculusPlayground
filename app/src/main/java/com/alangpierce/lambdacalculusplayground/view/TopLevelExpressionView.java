package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import autovalue.shaded.com.google.common.common.base.Preconditions;
import rx.Observable;

public class TopLevelExpressionView {
    private final DragObservableGenerator dragObservableGenerator;
    private final PointConverter pointConverter;
    private final RelativeLayout rootView;

    private ExpressionView exprView;
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
        Preconditions.checkState(exprView.getNativeView().getParent() == null,
                "Cannot attach an expression to the root that is already attached.");
        LinearLayout exprNativeView = exprView.getNativeView();
        DrawableAreaPoint drawableAreaPoint = pointConverter.toDrawableAreaPoint(canvasPos);
        rootView.addView(exprNativeView, Views.layoutParamsForRelativePos(drawableAreaPoint));
        invalidateExecuteButton(drawableAreaPoint);
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
            ExpressionView newExpression, DrawableAreaPoint drawableAreaPoint,
            boolean newIsExecutable) {
        rootView.removeView(exprView.getNativeView());
        rootView.addView(newExpression.getNativeView());
        exprView = newExpression;
        isExecutable = newIsExecutable;
        setCanvasPos(drawableAreaPoint);
    }

    public void decommission() {
        rootView.removeView(exprView.getNativeView());
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
