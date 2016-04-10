package com.alangpierce.lambdacalculusplayground.view;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class VariableView implements ExpressionView {
    private final LinearLayout view;

    public VariableView(LinearLayout view) {
        this.view = view;
    }

    public static VariableView render(ExpressionViewRenderer renderer, String varName) {
        LinearLayout mainView = renderer.makeExpressionViewWithChildren(
                ImmutableList.of(renderer.makeTextView(varName)));
        return new VariableView(mainView);
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
        view.setBackgroundColor(getColor(R.color.expression_highlight));
    }

    @Override
    public void handleDragExit() {
        view.setBackgroundColor(getColor(R.color.expression_background));
    }

    private @ColorInt
    int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(view.getContext(), resId);
    }
}
