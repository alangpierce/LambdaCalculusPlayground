package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

import autovalue.shaded.com.google.common.common.base.Preconditions;
import rx.Observable;

/**
 * Lambda views need to be able to expose the different subcomponents (parameter and body) and their
 * state.
 */
public class
        LambdaView implements ExpressionView {
    private final DragObservableGenerator dragObservableGenerator;
    private final ExpressionViewRenderer renderer;

    private final LinearLayout view;
    private final View parameterView;

    // If null, then we should present a placeholder view instead.
    private @Nullable ExpressionView bodyView;
    // Always non-null, and may be a placeholder.
    private View bodyNativeView;

    public LambdaView(
            DragObservableGenerator dragObservableGenerator, ExpressionViewRenderer renderer,
            LinearLayout view, View parameterView, @Nullable ExpressionView bodyView,
            View bodyNativeView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.renderer = renderer;
        this.view = view;
        this.parameterView = parameterView;
        this.bodyView = bodyView;
        this.bodyNativeView = bodyNativeView;
    }

    public static LambdaView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, String varName, @Nullable ExpressionView bodyView) {
        View parameterView = renderer.makeTextView(varName);
        View bodyNativeView =
                bodyView != null ? bodyView.getNativeView() : renderer.makeMissingBodyView();
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(ImmutableList.of(
                renderer.makeTextView("λ"), parameterView, bodyNativeView));
        return new LambdaView(dragObservableGenerator, renderer, mainView, parameterView, bodyView,
                bodyNativeView);
    }

    public Observable<? extends Observable<PointerMotionEvent>> getBodyObservable() {
        Preconditions.checkState(bodyView != null);
        return dragObservableGenerator.getDragObservable(bodyView.getNativeView());
    }

    public Observable<? extends Observable<PointerMotionEvent>> getParameterObservable() {
        return dragObservableGenerator.getDragObservable(parameterView);
    }

    @Override
    public LinearLayout getNativeView() {
        return view;
    }

    @Override
    public Point getScreenPos() {
        return Views.getScreenPos(view);
    }

    public void handleBodyChange(@Nullable ExpressionView newBody) {
        view.removeView(bodyNativeView);
        bodyView = newBody;
        if (newBody == null) {
            bodyNativeView = renderer.makeMissingBodyView();
        } else {
            bodyNativeView = newBody.getNativeView();
        }
        view.addView(bodyNativeView);
    }

    public Point getBodyPos() {
        return Views.getScreenPos(bodyNativeView);
    }
}
