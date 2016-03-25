package com.alangpierce.lambdacalculusplayground.view;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;
import rx.Observable;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import javax.annotation.Nullable;

/**
 * Lambda views need to be able to expose the different subcomponents (parameter and body) and their
 * state.
 */
public class LambdaView implements ExpressionView {
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
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(
                ImmutableList.of(
                        renderer.makeTextView("λ"),
                        parameterView,
                        renderer.makeBracketView("["),
                        bodyNativeView,
                        renderer.makeBracketView("]")));
        return new LambdaView(dragObservableGenerator, renderer, mainView, parameterView, bodyView,
                bodyNativeView);
    }

    public Observable<? extends Observable<PointerMotionEvent>> getWholeExpressionObservable() {
        return dragObservableGenerator.getDragObservable(view);
    }

    public @Nullable Observable<? extends Observable<PointerMotionEvent>> getBodyObservable() {
        if (bodyView == null) {
            return null;
        }
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
    public ScreenPoint getScreenPos() {
        return Views.getScreenPos(view);
    }

    public void handleBodyChange(@Nullable ExpressionView newBody) {
        view.removeView(bodyNativeView);
        bodyView = newBody;
        if (newBody == null) {
            bodyNativeView = renderer.makeMissingBodyView();
        } else {
            // We need to re-style the view because it may have changed to a top-level expression.
            bodyNativeView = renderer.styleLayout(newBody.getNativeView());
        }
        // The native view is at the position 3, after the lambda, the variable, and the open
        // bracket.
        view.addView(bodyNativeView, 3);
    }

    public ScreenPoint getBodyPos() {
        return Views.getScreenPos(bodyNativeView);
    }

    /**
     * Do a hit test to determine if the dragged view is over the body. If this lambda is part of
     * the dragged view, we want to return false.
     */
    public boolean bodyIntersectsWith(TopLevelExpressionView dragView) {
        LinearLayout dragNativeView = dragView.getNativeView();
        return !Views.isAncestor(bodyNativeView, dragNativeView) &&
                Views.viewsIntersect(bodyNativeView, dragNativeView);
    }

    public boolean parameterIntersectsWith(TopLevelExpressionView dragView) {
        return Views.viewsIntersect(parameterView, dragView.getNativeView());
    }

    public void handleBodyDragEnter() {
        bodyNativeView.setBackgroundColor(getColor(R.color.expression_highlight));
    }

    public void handleBodyDragExit() {
        bodyNativeView.setBackgroundColor(getColor(R.color.empty_body));
    }

    private int getColor(int resId) {
        return ContextCompat.getColor(bodyNativeView.getContext(), resId);
    }

    public int getBodyViewDepth() {
        return Views.viewDepth(bodyNativeView);
    }
}
