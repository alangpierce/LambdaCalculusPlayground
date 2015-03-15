package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragPacket;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import rx.Observable;

public class LambdaExpressionController implements ExpressionController {
    private final LambdaView view;

    private UserLambda userLambda;
    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;

    public LambdaExpressionController(LambdaView view, UserLambda userLambda) {
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
        public Observable<DragPacket> handleStartDrag(
                RelativeLayout rootView, Observable<PointerMotionEvent> dragEvents) {
            return dragEvents.map(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        onDetachCallback.onDetach(view.getNativeView());
                        setViewScreenPos(rootView, event.getScreenPos());
                        rootView.addView(view.getNativeView());
                        view.getNativeView().animate().setDuration(100)
                                .translationZBy(10).scaleX(1.05f).scaleY(1.05f);
                        break;
                    }
                    case MOVE: {
                        setViewScreenPos(rootView, event.getScreenPos());
                        break;
                    }
                    case UP:
                        view.getNativeView().animate().setDuration(100)
                                .translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
                        break;
                }
                return DragPacket.create(
                        Views.getBoundingBox(view.getNativeView()),
                        LambdaExpressionController.this);
            });
        }
        @Override
        public void handleCommit() {
            // TODO: Detach the data here and the view elsewhere.
        }
    }

    private class ParameterDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getParameterObservable();
        }
        @Override
        public Observable<DragPacket> handleStartDrag(RelativeLayout rootView,
                Observable<PointerMotionEvent> dragEvents) {
            return dragEvents.map(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        onDetachCallback.onDetach(view.getNativeView());
                        setViewScreenPos(rootView, event.getScreenPos());
                        rootView.addView(view.getNativeView());
                        view.getNativeView().animate().setDuration(100)
                                .translationZBy(10).scaleX(1.05f).scaleY(1.05f);
                        break;
                    }
                    case MOVE: {
                        setViewScreenPos(rootView, event.getScreenPos());
                        break;
                    }
                    case UP:
                        view.getNativeView().animate().setDuration(100)
                                .translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
                        break;
                }
                return DragPacket.create(
                        Views.getBoundingBox(view.getNativeView()),
                        LambdaExpressionController.this);
            });
        }
        @Override
        public void handleCommit() {
            // No further data changes needed.
        }
    }

    private void setViewScreenPos(RelativeLayout rootView, Point screenPos) {
        Point relativePos = screenPos.minus(Views.getScreenPos(rootView));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = relativePos.getX();
        layoutParams.topMargin = relativePos.getY();
        view.getNativeView().setLayoutParams(layoutParams);
    }
}
