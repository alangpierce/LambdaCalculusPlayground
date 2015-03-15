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
    public UserExpression getExpression() {
        return userLambda;
    }

    @Override
    public ExpressionView getView() {
        return view;
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

    public void handleBodyChange(@Nullable ExpressionController newBody) {
        userLambda = new UserLambda(
                userLambda.varName, newBody != null ? newBody.getExpression() : null);
        view.handleBodyChange(newBody != null ? newBody.getView() : null);
        if (newBody != null) {
            newBody.setOnChangeCallback(this::handleBodyChange);
        }
        onChangeCallback.onChange(this);
    }

    private class BodyDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return view.getBodyObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            ScreenExpression newScreenExpression = ScreenExpression.create(
                    userLambda.body, view.getBodyPos());
            subscription.unsubscribe();
            // This detaches the view from the UI, so it's safe to add the root view as a parent. It
            // also changes some class fields, so we need to grab them above.
            // TODO: Try to make things immutable to avoid this complexity.
            handleBodyChange(null);
            return controllerFactory.wrapInTopLevelController(bodyController, newScreenExpression);
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
