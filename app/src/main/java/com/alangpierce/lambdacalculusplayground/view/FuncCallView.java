package com.alangpierce.lambdacalculusplayground.view;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;
import rx.Observable;

import android.widget.LinearLayout;

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
        LinearLayout funcNativeView = renderer.styleLayout(funcView.getNativeView());
        LinearLayout argNativeView = renderer.styleLayout(argView.getNativeView());
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(
                ImmutableList.of(
                        funcNativeView,
                        renderer.makeBracketView("("),
                        argNativeView,
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

    public ScreenPoint getArgPos() {
        return Views.getScreenPos(argView.getNativeView());
    }

    public void handleFuncChange(ExpressionView newFuncView) {
        view.removeView(funcView.getNativeView());
        view.addView(newFuncView.getNativeView(), 0);
        funcView = newFuncView;
    }

    public void handleArgChange(ExpressionView newArgView) {
        view.removeView(argView.getNativeView());
        // The arg is at position 2, after the arg and the open paren.
        view.addView(newArgView.getNativeView(), 2);
        argView = newArgView;
    }
}
