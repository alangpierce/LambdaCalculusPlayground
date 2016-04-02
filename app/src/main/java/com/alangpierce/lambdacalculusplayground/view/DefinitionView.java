package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class DefinitionView {
    private final View nativeView;

    public DefinitionView(View nativeView) {
        this.nativeView = nativeView;
    }

    public static DefinitionView render(ExpressionViewRenderer renderer, RelativeLayout canvasRoot,
            String defName, DrawableAreaPoint point) {
        LinearLayout view = renderer.makeExpressionViewWithChildren(ImmutableList.of(
                renderer.makeTextView(defName),
                renderer.makeTextView(":="),
                renderer.makeMissingBodyView()
        ));
        view.setLayoutParams(Views.layoutParamsForRelativePos(point));
        canvasRoot.addView(view);
        return new DefinitionView(view);
    }

    public void setCanvasPos(DrawableAreaPoint pos) {
        nativeView.setLayoutParams(Views.layoutParamsForRelativePos(pos));
    }
}
