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
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class LambdaExpressionController implements ExpressionController {
    private final ExpressionControllerFactory controllerFactory;
    private final LambdaView view;

    private UserLambda userLambda;
    private @Nullable ExpressionController bodyController;
    private OnChangeCallback onChangeCallback;

    private final Subject<Observable<PointerMotionEvent>,Observable<PointerMotionEvent>>
            bodyDragActionSubject = PublishSubject.create();
    private @Nullable Subscription bodyDragActionSubscription;

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
        updateDragActionSubscription();
        return ImmutableList.of(new BodyDragSource(), new ParameterDragSource());
    }

    @Override
    public List<DropTarget> getDropTargets() {
        return ImmutableList.of();
    }

    public void handleBodyChange(@Nullable ExpressionController newBodyController) {
        userLambda = new UserLambda(
                userLambda.varName,
                newBodyController != null ? newBodyController.getExpression() : null);
        view.handleBodyChange(newBodyController != null ? newBodyController.getView() : null);
        updateDragActionSubscription();
        if (newBodyController != null) {
            newBodyController.setOnChangeCallback(this::handleBodyChange);
        }
        bodyController = newBodyController;
        onChangeCallback.onChange(this);
    }

    private void updateDragActionSubscription() {
        if (bodyDragActionSubscription != null) {
            bodyDragActionSubscription.unsubscribe();
            bodyDragActionSubscription = null;
        }
        @Nullable Observable<? extends Observable<PointerMotionEvent>> bodyObservable =
                view.getBodyObservable();
        if (bodyObservable != null) {
            bodyDragActionSubscription = bodyObservable.subscribe(bodyDragActionSubject);
        }
    }

    private class BodyDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return bodyDragActionSubject;
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            ScreenExpression newScreenExpression = ScreenExpression.create(
                    userLambda.body, view.getBodyPos());
            ExpressionController controllerToDrag = bodyController;
            subscription.unsubscribe();
            // This detaches the view from the UI, so it's safe to add the root view as a parent. It
            // also changes some class fields, so we need to grab them above.
            // TODO: Try to make things immutable to avoid this complexity.
            handleBodyChange(null);
            return controllerFactory.wrapInTopLevelController(
                    controllerToDrag, newScreenExpression);
        }
    }

    private class ParameterDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            // The parameter shouldn't ever change, so no need to use a subject.
            return view.getParameterObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag(Subscription subscription) {
            return controllerFactory.createTopLevelController(ScreenExpression.create(
                    new UserVariable(userLambda.varName), view.getScreenPos()));
        }
    }
}
