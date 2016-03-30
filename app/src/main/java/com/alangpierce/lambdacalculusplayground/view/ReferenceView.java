package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class ReferenceView implements ExpressionView {
    private final LinearLayout view;

    public ReferenceView(LinearLayout view) {
        this.view = view;
    }

    public static ReferenceView render(ExpressionViewRenderer renderer, String defName) {
        LinearLayout mainView = renderer.makeExpressionViewWithChildren(
                ImmutableList.of(renderer.makeTextView(defName)));
        return new ReferenceView(mainView);
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
