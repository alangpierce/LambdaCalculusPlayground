package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class TopLevelExpressionControllerImpl implements TopLevelExpressionController {
    private final TopLevelExpressionView view;
    private final RelativeLayout rootView;

    private ScreenExpression screenExpression;
    private ExpressionController expressionController;
    private OnTopLevelChangeCallback onChangeCallback;

    private final Subject<Observable<PointerMotionEvent>,Observable<PointerMotionEvent>>
            dragActionSubject = PublishSubject.create();
    private @Nullable Subscription dragActionSubscription;

    public TopLevelExpressionControllerImpl(
            TopLevelExpressionView view, RelativeLayout rootView,
            ScreenExpression screenExpression, ExpressionController expressionController) {
        this.view = view;
        this.rootView = rootView;
        this.screenExpression = screenExpression;
        this.expressionController = expressionController;
    }

    @Override
    public ScreenExpression getScreenExpression() {
        return screenExpression;
    }

    @Override
    public TopLevelExpressionView getView() {
        return view;
    }

    @Override
    public void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public void handlePositionChange(Point screenPos) {
        Point canvasPos = screenPos.minus(Views.getScreenPos(rootView));
        screenExpression = ScreenExpression.create(screenExpression.getExpr(), canvasPos);
        onChangeCallback.onChange(this);
    }

    public void handleExprChange(ExpressionController newExpressionController) {
        UserExpression newExpression = newExpressionController.getExpression();
        screenExpression = ScreenExpression.create(newExpression, screenExpression.getCanvasPos());
        view.handleExpressionChange(
                newExpressionController.getView(), screenExpression.getCanvasPos());
        newExpressionController.setOnChangeCallback(this::handleExprChange);
        expressionController = newExpressionController;
        updateDragActionSubscription();
        onChangeCallback.onChange(this);
    }

    private void updateDragActionSubscription() {
        if (dragActionSubscription != null) {
            dragActionSubscription.unsubscribe();
            dragActionSubscription = null;
        }
        dragActionSubscription = view.getExpressionObservable().subscribe(dragActionSubject);
    }

    @Override
    public List<DragSource> getDragSources() {
        updateDragActionSubscription();
        return ImmutableList.of(new TopLevelExpressionDragSource());
    }

    @Override
    public ExpressionController decommission() {
        onChangeCallback.onChange(null);
        if (dragActionSubscription != null) {
            dragActionSubscription.unsubscribe();
        }
        view.decommission();
        return expressionController;
    }

    private class TopLevelExpressionDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return dragActionSubject;
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            return TopLevelExpressionControllerImpl.this;
        }
    }
}
