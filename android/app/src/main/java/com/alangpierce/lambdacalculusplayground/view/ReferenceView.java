package com.alangpierce.lambdacalculusplayground.view;

import android.support.annotation.DrawableRes;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class ReferenceView implements ExpressionView {
    private final LinearLayout view;

    private @DrawableRes int background = R.drawable.expression_background;

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

    public void setValid(boolean isValid) {
        if (isValid) {
            background = R.drawable.expression_background;
        } else {
            background = R.drawable.invalid_reference;
        }
        view.setBackgroundResource(background);
    }

    @Override
    public void handleDragEnter() {
        view.setBackgroundResource(R.drawable.expression_highlight);
    }

    @Override
    public void handleDragExit() {
        view.setBackgroundResource(background);
    }
}
