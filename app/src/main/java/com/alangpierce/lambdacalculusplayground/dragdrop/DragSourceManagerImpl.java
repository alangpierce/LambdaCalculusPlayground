package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;

import java.util.Collections;
import java.util.List;

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
            /*
             * TODO: We're assuming that the observable doesn't even get created until the drag
             * action starts, which might not be true.
             */
            TopLevelExpressionController expressionController = dragSource.handleStartDrag();
            LinearLayout view = expressionController.getView().getNativeView();
            rootView.addView(view);
            expressionController.setCallbacks(
                    // onChange
                    // TODO: Register the callback correctly.
                    (newScreenExpression) -> {},
//                    (newScreenExpression) ->
//                            expressionState.modifyExpression(exprId, newScreenExpression),
                    // onDetach
                    rootView::removeView);

            dragEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN:
                        view.animate().setDuration(100)
                                .translationZBy(10).scaleX(1.05f).scaleY(1.05f);
                        // Fall through.
                    case MOVE: {
                        setViewScreenPos(view, event.getScreenPos());
                        break;
                    }
                    case UP:
                        view.animate().setDuration(100)
                                .translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
                        break;
                }
            });
        });
    }

    private void setViewScreenPos(LinearLayout view, Point screenPos) {
        Point relativePos = screenPos.minus(Views.getScreenPos(rootView));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = relativePos.getX();
        layoutParams.topMargin = relativePos.getY();
        view.setLayoutParams(layoutParams);
    }
}
