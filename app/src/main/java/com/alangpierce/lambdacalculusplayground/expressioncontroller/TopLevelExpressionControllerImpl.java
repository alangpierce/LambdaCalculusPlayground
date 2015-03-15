package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;

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
    public TopLevelExpressionView getView() {
        return view;
    }

    @Override
    public void setOnChangeCallback(OnTopLevelChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    public void handleExprChange(UserExpression userExpression) {
        screenExpression =
                ScreenExpression.create(userExpression, screenExpression.getScreenCoords());
        onChangeCallback.onChange(screenExpression);
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
        public TopLevelExpressionController handleStartDrag() {
            view.detachFromRoot();
            return TopLevelExpressionControllerImpl.this;
        }
    }
}
