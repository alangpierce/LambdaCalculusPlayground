package com.alangpierce.lambdacalculusplayground.view;

import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.google.common.collect.ImmutableList;

import rx.Observable;

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

    public Observable<? extends Observable<PointerMotionEvent>> getWholeViewObservable() {
        return dragObservableGenerator.getDragObservable(view);
    }

    @Override
    public LinearLayout getNativeView() {
        return view;
    }
}
