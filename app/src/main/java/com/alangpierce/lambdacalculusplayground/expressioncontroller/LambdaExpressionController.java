package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragPacket;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;

import rx.Observable;

public class LambdaExpressionController implements ExpressionController {
    private final LinearLayout view;

    private UserLambda userLambda;
    private OnChangeCallback onChangeCallback;
    private OnDetachCallback onDetachCallback;

    public LambdaExpressionController(LinearLayout view, UserLambda userLambda) {
        this.view = view;
        this.userLambda = userLambda;
    }

    @Override
    public void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback) {
        this.onChangeCallback = onChangeCallback;
        this.onDetachCallback = onDetachCallback;
    }

    @Override
    public LinearLayout getView() {
        return view;
    }

    public void handleBodyDetach(View viewToDetach) {
        view.removeView(viewToDetach);
        handleBodyChange(null);
    }

    public void handleBodyChange(UserExpression newBody) {
        userLambda = new UserLambda(userLambda.varName, newBody);
        onChangeCallback.onChange(userLambda);
    }

    public DragSource getDragSource() {
        return new DragSource() {
            @Override
            public View getDragSourceView() {
                return view;
            }
            @Override
            public Observable<DragPacket> handleStartDrag(
                    RelativeLayout rootView, Observable<PointerMotionEvent> dragEvents) {
                return dragEvents.map(event -> {
                    switch (event.getAction()) {
                        case DOWN: {
                            onDetachCallback.onDetach(view);
                            setViewScreenPos(rootView, event.getScreenPos());
                            rootView.addView(view);
                            view.animate().setDuration(100)
                                    .translationZBy(10).scaleX(1.05f).scaleY(1.05f);
                            break;
                        }
                        case MOVE: {
                            setViewScreenPos(rootView, event.getScreenPos());
                            break;
                        }
                        case UP:
                            view.animate().setDuration(100)
                                    .translationZBy(-10).scaleX(1.0f).scaleY(1.0f);
                            break;
                    }
                    return DragPacket.create(
                            Views.getBoundingBox(view), LambdaExpressionController.this);
                });
            }
            @Override
            public void handleCommit() {
                // TODO: Detach the data here and the view elsewhere.
            }
        };
    }

    private void setViewScreenPos(RelativeLayout rootView, Point screenPos) {
        Point relativePos = screenPos.minus(Views.getScreenPos(rootView));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = relativePos.getX();
        layoutParams.topMargin = relativePos.getY();
        view.setLayoutParams(layoutParams);
    }
}
