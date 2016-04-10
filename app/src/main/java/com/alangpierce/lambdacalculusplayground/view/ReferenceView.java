package com.alangpierce.lambdacalculusplayground.view;

import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class ReferenceView implements ExpressionView {
    private final LinearLayout view;

    private @ColorRes int backgroundColor = R.color.expression_background;

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
            backgroundColor = R.color.expression_background;
        } else {
            backgroundColor = R.color.invalid_reference;
        }
        view.setBackgroundColor(getColor(backgroundColor));
    }

    @Override
    public void handleDragEnter() {
        view.setBackgroundColor(getColor(R.color.expression_highlight));
    }

    @Override
    public void handleDragExit() {
        view.setBackgroundColor(getColor(backgroundColor));
    }

    private @ColorInt int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(view.getContext(), resId);
    }
}
