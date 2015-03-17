package com.alangpierce.lambdacalculusplayground.view;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

public class VariableView implements ExpressionView {
    private final DragObservableGenerator dragObservableGenerator;

    private final LinearLayout view;

    public VariableView(DragObservableGenerator dragObservableGenerator, LinearLayout view) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.view = view;
    }

    public static VariableView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, String varName) {
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(
                ImmutableList.of(renderer.makeTextView(varName)));
        return new VariableView(dragObservableGenerator, mainView);
    }

    @Override
    public LinearLayout getNativeView() {
        return view;
    }

    @Override
    public Point getScreenPos() {
        return Views.getScreenPos(view);
    }
}
