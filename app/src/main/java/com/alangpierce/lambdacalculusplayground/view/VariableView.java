package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class VariableView implements ExpressionView {
    private final LinearLayout view;

    public VariableView(LinearLayout view) {
        this.view = view;
    }

    public static VariableView render(ExpressionViewRenderer renderer, String varName) {
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(
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
}
