package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

import autovalue.shaded.com.google.common.common.base.Preconditions;
import rx.Observable;

public class FuncCallView implements ExpressionView {
    private final DragObservableGenerator dragObservableGenerator;

    private final LinearLayout view;

    private ExpressionView funcView;
    private ExpressionView argView;

    public FuncCallView(
            DragObservableGenerator dragObservableGenerator, LinearLayout view,
            ExpressionView funcView,
            ExpressionView argView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.view = view;
        this.funcView = funcView;
        this.argView = argView;
    }

    public static FuncCallView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, ExpressionView funcView, ExpressionView argView) {
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(
                ImmutableList.of(funcView.getNativeView(), argView.getNativeView()));
        return new FuncCallView(dragObservableGenerator, mainView, funcView, argView);
    }

    @Override
    public LinearLayout getNativeView() {
        return view;
    }

    @Override
    public Point getScreenPos() {
        return Views.getScreenPos(view);
    }

    public Observable<? extends Observable<PointerMotionEvent>> getArgObservable() {
        return dragObservableGenerator.getDragObservable(argView.getNativeView());
    }

    public ExpressionView detachArg() {
        Preconditions.checkState(argView.getNativeView().getParent() == view);
        view.removeView(argView.getNativeView());
        return argView;
    }
}
