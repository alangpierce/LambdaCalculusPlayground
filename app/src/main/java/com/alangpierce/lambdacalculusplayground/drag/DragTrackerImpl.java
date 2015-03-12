package com.alangpierce.lambdacalculusplayground.drag;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent.Action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import autovalue.shaded.com.google.common.common.base.Throwables;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

public class DragTrackerImpl implements DragTracker {
    private final DragObservableGenerator dragObservableGenerator;

    private Point lastPos;
    private boolean isActive = false;
    private View dragView;

    public DragTrackerImpl(DragObservableGenerator dragObservableGenerator) {
        this.dragObservableGenerator = dragObservableGenerator;
    }

    @Override
    public void registerDraggableView(final View view, final StartDragHandler handler) {
        Observable<? extends Observable<PointerMotionEvent>> dragEvents =
                dragObservableGenerator.getDragObservable(view);
        dragEvents.subscribe(dragObservable -> {
            // Ignore drag events that come in while one is in progress.
            if (isActive) {
                dragObservable.take(0);
                return;
            }
            isActive = true;
            dragObservable.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        lastPos = event.getScreenPos();
                        dragView = handler.onStartDrag();
                        break;
                    }
                    case MOVE: {
                        Point pos = event.getScreenPos();
                        moveView(pos.getX() - lastPos.getX(), pos.getY() - lastPos.getY());
                        lastPos = pos;
                        break;
                    }
                    case UP:
                        isActive = false;
                        break;
                }
            });
        });
    }

    void moveView(int dx, int dy) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)dragView.getLayoutParams();
        layoutParams.leftMargin += dx;
        layoutParams.topMargin += dy;
        dragView.setLayoutParams(layoutParams);
    }
}
