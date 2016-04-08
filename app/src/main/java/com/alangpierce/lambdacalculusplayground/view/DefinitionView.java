package com.alangpierce.lambdacalculusplayground.view;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.compat.Compat;
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
    private final RelativeLayout canvasRoot;

    public DefinitionView(
            DragObservableGenerator dragObservableGenerator, ProducerView referenceProducerView,
            SlotView expressionSlotView, View nativeView, RelativeLayout canvasRoot) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.referenceProducerView = referenceProducerView;
        this.expressionSlotView = expressionSlotView;
        this.nativeView = nativeView;
        this.canvasRoot = canvasRoot;
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
                expressionSlotView, view, canvasRoot);
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

    public View getNativeView() {
        return nativeView;
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

    public void startDrag() {
        canvasRoot.bringChildToFront(nativeView);
        ViewPropertyAnimator animator = nativeView.animate()
                .setDuration(100).scaleX(1.05f).scaleY(1.05f);
        Compat.translationZBy(animator, 10);
    }

    public void endDrag() {
        ViewPropertyAnimator animator = nativeView.animate()
                .setDuration(100).scaleX(1.0f).scaleY(1.0f);
        Compat.translationZBy(animator, -10);
    }
}
