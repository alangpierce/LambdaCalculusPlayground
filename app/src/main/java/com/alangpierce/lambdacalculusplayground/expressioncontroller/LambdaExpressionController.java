package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
import com.alangpierce.lambdacalculusplayground.TopLevelExpressionCreator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;

public class LambdaExpressionController implements ExpressionController {
    private final ExpressionControllerFactory controllerFactory;
    private final LambdaView view;

    private UserLambda userLambda;
    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;

    public LambdaExpressionController(
            ExpressionControllerFactory controllerFactory,
            LambdaView view,
            UserLambda userLambda) {
        this.controllerFactory = controllerFactory;
        this.view = view;
        this.userLambda = userLambda;
    }

    @Override
    public void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }

    @Override
    public List<DragSource> getDragSources() {
        return ImmutableList.of(new BodyDragSource(), new ParameterDragSource());
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of();
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    public void handleBodyDetach(View viewToDetach) {
        view.getNativeView().removeView(viewToDetach);
        handleBodyChange(null);
    }

    public void handleBodyChange(UserExpression newBody) {
        userLambda = new UserLambda(userLambda.varName, newBody);
        onChangeCallback.onChange(userLambda);
    }

    private class BodyDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getWholeViewObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            onDetachCallback.onDetach(view.getNativeView());
            TopLevelExpressionControllerImpl result =
                    new TopLevelExpressionControllerImpl(
                            view, ScreenExpression.create(userLambda, view.getScreenPos()));
            setCallbacks(result::handleExprChange, result::handleExprDetach);
            return result;
        }
    }

    private class ParameterDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getParameterObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            return controllerFactory.createTopLevelController(ScreenExpression.create(
                    new UserVariable(userLambda.varName), view.getScreenPos()));
        }
    }
}
