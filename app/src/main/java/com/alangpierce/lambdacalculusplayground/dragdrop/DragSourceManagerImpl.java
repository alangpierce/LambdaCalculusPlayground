package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import autovalue.shaded.com.google.common.common.collect.Lists;
import rx.Observable;

public class DragSourceManagerImpl implements DragManager {
    private final RelativeLayout rootView;

    private final List<DragSource> dragSources = Collections.synchronizedList(Lists.newArrayList());
    private final List<DropTarget> dropTargets = Collections.synchronizedList(Lists.newArrayList());

    public DragSourceManagerImpl(RelativeLayout rootView) {
        this.rootView = rootView;
    }

    @Override
    public void registerDragSource(DragSource dragSource) {
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragSource.getDragObservable();

        dragObservable.subscribe(dragEvents -> {
            AtomicReference<LinearLayout> viewReference = new AtomicReference<>();
            dragEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        TopLevelExpressionController expressionController = dragSource.handleStartDrag();
                        LinearLayout view = expressionController.getView().getNativeView();
                        viewReference.set(view);
                        Point screenPos = expressionController.getView().getScreenPos();
                        rootView.addView(view, Views.layoutParamsForScreenPosition(
                                rootView, screenPos));
                        expressionController.setOnChangeCallback(
                                // onChange
                                // TODO: Register the callback correctly.
                                (newScreenExpression) -> {
                                });

                        view.animate().setDuration(100)
                                .translationZBy(10).scaleX(1.05f).scaleY(1.05f);
                    }
                    case MOVE: {
                        LinearLayout view = viewReference.get();
                        if (view == null) {
                            break;
                        }
                        view.setLayoutParams(Views.layoutParamsForScreenPosition(
                                rootView, event.getScreenPos()));
                        break;
                    }
                    case UP:
                        LinearLayout view = viewReference.get();
                        if (view == null) {
                            break;
                        }
                        view.animate().setDuration(100)
                                .translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
                        break;
                }
            });
        });
    }
}
