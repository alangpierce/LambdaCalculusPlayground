package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

import rx.Observable;

public class DefinitionView {
    private final DragObservableGenerator dragObservableGenerator;

    private final View referenceView;
    private final View nativeView;

    public DefinitionView(
            DragObservableGenerator dragObservableGenerator, View referenceView,
            View nativeView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.referenceView = referenceView;
        this.nativeView = nativeView;
    }

    public static DefinitionView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, RelativeLayout canvasRoot, String defName,
            DrawableAreaPoint point) {
        View referenceView = renderer.makeTextView(defName);
        LinearLayout view = renderer.makeExpressionViewWithChildren(ImmutableList.of(
                referenceView,
                renderer.makeTextView(":="),
                renderer.makeMissingBodyView()
        ));
        view.setLayoutParams(Views.layoutParamsForRelativePos(point));
        canvasRoot.addView(view);
        return new DefinitionView(dragObservableGenerator, referenceView, view);
    }

    public void setCanvasPos(DrawableAreaPoint pos) {
        nativeView.setLayoutParams(Views.layoutParamsForRelativePos(pos));
    }

    public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
        return dragObservableGenerator.getDragObservable(nativeView);
    }

    public Observable<? extends Observable<PointerMotionEvent>> getReferenceObservable() {
        return dragObservableGenerator.getDragObservable(referenceView);
    }

    public ScreenPoint getReferencePos() {
        return Views.getScreenPos(referenceView);
    }
}
