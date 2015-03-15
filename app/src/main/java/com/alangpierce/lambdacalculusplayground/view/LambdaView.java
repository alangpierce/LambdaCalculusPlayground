package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

import rx.Observable;

/**
 * Lambda views need to be able to expose the different subcomponents (parameter and body) and their
 * state.
 */
public class LambdaView implements ExpressionView {
    private final DragObservableGenerator dragObservableGenerator;

    private final LinearLayout view;
    private final View parameterView;

    // If null, then we should present a placeholder view instead.
    private @Nullable LinearLayout bodyView;

    public LambdaView(
            DragObservableGenerator dragObservableGenerator, LinearLayout view,
            View parameterView, LinearLayout bodyView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.view = view;
        this.parameterView = parameterView;
        this.bodyView = bodyView;
    }

    public static LambdaView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, String varName, @Nullable LinearLayout bodyView) {
        View parameterView = renderer.makeTextView(varName);
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(ImmutableList.of(
                renderer.makeTextView("λ"),
                parameterView,
                bodyView != null ? bodyView : renderer.makeMissingBodyView()));
        return new LambdaView(dragObservableGenerator, mainView, parameterView, bodyView);
    }

    public Observable<? extends Observable<PointerMotionEvent>> getWholeViewObservable() {
        return dragObservableGenerator.getDragObservable(view);
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
}
