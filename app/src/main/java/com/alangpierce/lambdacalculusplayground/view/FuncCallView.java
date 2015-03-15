package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

import rx.Observable;

public class FuncCallView implements ExpressionView {
    private final DragObservableGenerator dragObservableGenerator;

    private final LinearLayout view;

    public FuncCallView(
            DragObservableGenerator dragObservableGenerator, LinearLayout view) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.view = view;
    }

    public static FuncCallView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, LinearLayout func, LinearLayout arg) {
        LinearLayout mainView = renderer.makeLinearLayoutWithChildren(ImmutableList.of(func, arg));
        return new FuncCallView(dragObservableGenerator, mainView);
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
