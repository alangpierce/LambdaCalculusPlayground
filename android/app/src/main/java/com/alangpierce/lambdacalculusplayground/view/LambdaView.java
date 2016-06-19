package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.R;
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
    private final LinearLayout view;

    public LambdaView(LinearLayout view) {
        this.view = view;
    }

    public static LambdaView render(ExpressionViewRenderer renderer, String varName,
                                    @Nullable ExpressionView bodyView) {
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
        return new LambdaView(mainView);
    }

    @Override
    public LinearLayout getNativeView() {
        return view;
    }

    @Override
    public ScreenPoint getScreenPos() {
        return Views.getScreenPos(view);
    }

    @Override
    public void handleDragEnter() {
        view.setBackgroundResource(R.drawable.expression_highlight);
    }

    @Override
    public void handleDragExit() {
        view.setBackgroundResource(R.drawable.expression_background);
    }
}
