package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.component.ComponentParent;
import com.alangpierce.lambdacalculusplayground.component.ProducerView;
import com.alangpierce.lambdacalculusplayground.component.SlotView;
import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;

import rx.Observable;

public class DefinitionView {
    private final DragObservableGenerator dragObservableGenerator;

    private final ProducerView referenceProducerView;
    private final SlotView expressionSlotView;
    private final View nativeView;

    public DefinitionView(
            DragObservableGenerator dragObservableGenerator, ProducerView referenceProducerView,
            SlotView expressionSlotView, View nativeView) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.referenceProducerView = referenceProducerView;
        this.expressionSlotView = expressionSlotView;
        this.nativeView = nativeView;
    }

    public static DefinitionView render(DragObservableGenerator dragObservableGenerator,
            ExpressionViewRenderer renderer, RelativeLayout canvasRoot, String defName,
            @Nullable ExpressionView expressionView, DrawableAreaPoint point) {
        View referenceView = renderer.makeTextView(defName);
        View expressionNativeView = expressionView != null ?
                expressionView.getNativeView() : renderer.makeMissingBodyView();
        LinearLayout view = renderer.makeExpressionViewWithChildren(ImmutableList.of(
                referenceView,
                renderer.makeDefinitionView(),
                expressionNativeView
        ));
        view.setLayoutParams(Views.layoutParamsForRelativePos(point));
        canvasRoot.addView(view);
        ProducerView referenceProducerView =
                new ProducerView(dragObservableGenerator, referenceView);
        SlotView expressionSlotView =
                new SlotView(dragObservableGenerator, renderer, expressionComponentParent(view),
                        expressionView, expressionNativeView);
        return new DefinitionView(dragObservableGenerator, referenceProducerView,
                expressionSlotView, view);
    }

    public static ComponentParent expressionComponentParent(LinearLayout mainView) {
        return new ComponentParent() {
            @Override
            public void detach(View view) {
                mainView.removeView(view);
            }
            @Override
            public void attach(View view) {
                // The expression is at the end, so we don't need to worry about the index.
                mainView.addView(view);
            }
        };
    }

    public void setCanvasPos(DrawableAreaPoint pos) {
        nativeView.setLayoutParams(Views.layoutParamsForRelativePos(pos));
    }

    public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
        return dragObservableGenerator.getDragObservable(nativeView);
    }

    public ProducerView getReferenceProducer() {
        return referenceProducerView;
    }

    public SlotView getExpressionSlot() {
        return expressionSlotView;
    }
}
