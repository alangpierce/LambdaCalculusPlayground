package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;
import com.alangpierce.lambdacalculusplayground.view.ExpressionView;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class LambdaExpressionController implements ExpressionController {
    private final TopLevelExpressionManager topLevelExpressionManager;
    private final LambdaView view;

    private UserLambda userLambda;
    private @Nullable ExpressionController bodyController;
    private OnChangeCallback onChangeCallback;

    private final Subject<Observable<PointerMotionEvent>,Observable<PointerMotionEvent>>
            bodyDragActionSubject = PublishSubject.create();
    private @Nullable Subscription bodyDragActionSubscription;

    public LambdaExpressionController(
            TopLevelExpressionManager topLevelExpressionManager, LambdaView view,
            UserLambda userLambda,
            @Nullable ExpressionController bodyController) {
        this.topLevelExpressionManager = topLevelExpressionManager;
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
        return ImmutableList.of(new BodyDropTarget());
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
        public TopLevelExpressionController handleStartDrag() {
            Point screenPos = view.getBodyPos();
            ExpressionController controllerToDrag = bodyController;
            // This detaches the view from the UI, so it's safe to add the root view as a parent. It
            // also changes some class fields, so we need to grab them above.
            // TODO: Try to make things immutable to avoid this complexity.
            handleBodyChange(null);
            return topLevelExpressionManager.sendExpressionToTopLevel(controllerToDrag, screenPos);
        }
    }

    private class ParameterDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            // The parameter shouldn't ever change, so no need to use a subject.
            return view.getParameterObservable();
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            return topLevelExpressionManager.createNewExpression(
                    new UserVariable(userLambda.varName), view.getScreenPos());
        }
    }

    private class BodyDropTarget implements DropTarget {
        @Override
        public boolean hitTest(TopLevelExpressionView dragView) {
            return userLambda.body == null && view.bodyIntersectsWith(dragView);
        }
        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            view.handleDragEnter();
        }
        @Override
        public void handleExit() {
            // Don't change our display unless we're actually accepting drops.
            if (userLambda.body != null) {
                return;
            }
            view.handleDragExit();
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            view.handleDragExit();
            ExpressionController bodyController = expressionController.decommission();
            handleBodyChange(bodyController);
        }
    }
}
