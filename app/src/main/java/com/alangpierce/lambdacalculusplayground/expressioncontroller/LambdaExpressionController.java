package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;
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

import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscription;

public class LambdaExpressionController implements ExpressionController {
    private final ExpressionControllerFactory controllerFactory;
    private final LambdaView view;

    private UserLambda userLambda;
    private @Nullable ExpressionController bodyController;
    private OnChangeCallback onChangeCallback;

    public LambdaExpressionController(
            ExpressionControllerFactory controllerFactory,
            LambdaView view,
            UserLambda userLambda,
            @Nullable ExpressionController bodyController) {
        this.controllerFactory = controllerFactory;
        this.view = view;
        this.userLambda = userLambda;
        this.bodyController = bodyController;
    }

    @Override
    public void setOnChangeCallback(OnChangeCallback onChangeCallback) {
        this.onChangeCallback = onChangeCallback;
    }

    @Override
    public List<DragSource> getDragSources() {
        ImmutableList.Builder<DragSource> resultBuilder = ImmutableList.builder();
        if (userLambda.body != null) {
            resultBuilder.add(new BodyDragSource());
        }
        resultBuilder.add(new ParameterDragSource());
        return resultBuilder.build();
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of();
    }

    @Override
    public ExpressionView getView() {
        return view;
    }

    public void handleBodyChange(UserExpression newBody) {
        userLambda = new UserLambda(userLambda.varName, newBody);
        onChangeCallback.onChange(userLambda);
    }

    private class BodyDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getBodyObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            subscription.unsubscribe();
            ExpressionView bodyView = view.detachBody();
            // TODO: Call handleBodyChange(null) in a way that works.
            return controllerFactory.wrapInTopLevelController(
                    bodyController,
                    ScreenExpression.create(userLambda.body, bodyView.getScreenPos()));
        }
    }

    private class ParameterDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getParameterObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            return controllerFactory.createTopLevelController(ScreenExpression.create(
                    new UserVariable(userLambda.varName), view.getScreenPos()));
        }
    }
}
