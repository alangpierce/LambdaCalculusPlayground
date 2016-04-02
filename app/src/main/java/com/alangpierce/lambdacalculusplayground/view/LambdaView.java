package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.component.ComponentParent;
import com.alangpierce.lambdacalculusplayground.component.SlotView;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
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
    private final SlotView bodySlotView;

    public LambdaView(DragObservableGenerator dragObservableGenerator, LinearLayout view,
            View parameterView, SlotView bodySlotView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.view = view;
        this.parameterView = parameterView;
        this.bodySlotView = bodySlotView;
    }

    public static LambdaView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, String varName, @Nullable ExpressionView bodyView) {
        View parameterView = renderer.makeTextView(varName);
        View bodyNativeView =
                bodyView != null ? bodyView.getNativeView() : renderer.makeMissingBodyView();
        LinearLayout mainView = renderer.makeExpressionViewWithChildren(
                ImmutableList.of(
                        renderer.makeTextView("λ"),
                        parameterView,
                        renderer.makeBracketView("["),
                        bodyNativeView,
                        renderer.makeBracketView("]")));
        SlotView bodySlotView = new SlotView(dragObservableGenerator, renderer,
                bodyComponentParent(mainView), bodyView, bodyNativeView);
        return new LambdaView(dragObservableGenerator, mainView, parameterView, bodySlotView);
    }

    public static ComponentParent bodyComponentParent(LinearLayout mainView) {
        return new ComponentParent() {
            @Override
            public void detach(View view) {
                mainView.removeView(view);
            }
            @Override
            public void attach(View view) {
                // Add the view after the lambda, the variable, and the open bracket.
                mainView.addView(view, 3);
            }
        };
    }

    public SlotView getBodySlot() {
        return bodySlotView;
    }

    public Observable<? extends Observable<PointerMotionEvent>> getWholeExpressionObservable() {
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
    public ScreenPoint getScreenPos() {
        return Views.getScreenPos(view);
    }

    public ScreenPoint getParameterPos() {
        return Views.getScreenPos(parameterView);
    }

    public boolean parameterIntersectsWith(TopLevelExpressionView dragView) {
        return Views.viewsIntersect(parameterView, dragView.getNativeView());
    }
}
