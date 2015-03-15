package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;
import rx.Subscription;

public class TopLevelExpressionControllerImpl implements TopLevelExpressionController {
    private final TopLevelExpressionView view;

    private ScreenExpression screenExpression;
    private OnTopLevelChangeCallback onChangeCallback;

    public TopLevelExpressionControllerImpl(
            TopLevelExpressionView view,
            ScreenExpression screenExpression) {
        this.view = view;
        this.screenExpression = screenExpression;
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

    public void handleExprChange(ExpressionController newExpressionController) {
        UserExpression newExpression = newExpressionController.getExpression();
        screenExpression =
                ScreenExpression.create(newExpression, screenExpression.getScreenCoords());
        view.handleExpressionChange(newExpressionController.getView());
        newExpressionController.setOnChangeCallback(this::handleExprChange);
        onChangeCallback.onChange(this);
    }

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of(new TopLevelExpressionDragSource());
    }

    private class TopLevelExpressionDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getExpressionObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            view.detachFromRoot();
            return TopLevelExpressionControllerImpl.this;
        }
    }
}
