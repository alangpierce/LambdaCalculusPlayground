package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

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
        LinearLayout mainView = renderer.makeExpressionViewWithChildren(
                ImmutableList.of(
                        funcView.getNativeView(),
                        renderer.makeBracketView("("),
                        argView.getNativeView(),
                        renderer.makeBracketView(")")));
        return new FuncCallView(dragObservableGenerator, mainView, funcView, argView);
    }

    @Override
    public LinearLayout getNativeView() {
        return view;
    }

    @Override
    public ScreenPoint getScreenPos() {
        return Views.getScreenPos(view);
    }

    public Observable<? extends Observable<PointerMotionEvent>> getArgObservable() {
        return dragObservableGenerator.getDragObservable(argView.getNativeView());
    }

    /**
     * Tear apart this view because the arg is being dragged out. The func needs to be detached so
     * that it can be attached to a different view later, and the view itself should be removed so
     * it doesn't stick around on the screen.
     */
    public void decommission() {
        view.removeView(funcView.getNativeView());
        view.removeView(argView.getNativeView());
    }

    public void detachFunc() {
        view.removeView(funcView.getNativeView());
        funcView = null;
    }

    public void attachFunc(ExpressionView newFuncView) {
        Preconditions.checkState(funcView == null);
        view.addView(newFuncView.getNativeView(), 0);
        funcView = newFuncView;
    }

    public void detachArg() {
        view.removeView(argView.getNativeView());
        argView = null;
    }

    public void attachArg(ExpressionView newArgView) {
        Preconditions.checkState(argView == null);
        // The arg is at position 2, after the arg and the open paren.
        view.addView(newArgView.getNativeView(), 2);
        argView = newArgView;
    }
}
