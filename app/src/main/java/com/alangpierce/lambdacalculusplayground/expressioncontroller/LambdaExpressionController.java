package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.FuncCallDropTarget.FuncCallControllerFactory;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
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
    private final TopLevelExpressionManager topLevelExpressionManager;
    private final LambdaView view;

    private UserLambda userLambda;
    private @Nullable ExpressionController bodyController;
    private OnChangeCallback onChangeCallback;

    private final Subject<Observable<PointerMotionEvent>, Observable<PointerMotionEvent>>
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
    public OnChangeCallback getOnChangeCallback() {
        return onChangeCallback;
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
    public List<DropTarget> getDropTargets(FuncCallControllerFactory funcCallFactory) {
        return ImmutableList.of(
                new BodyDropTarget(),
                new FuncCallDropTarget(this, view, funcCallFactory),
                new ParameterDropTarget());
    }

    // Note that the returned body might be null.
    public void handleBodyChange(ExpressionControllerProvider newBodyControllerProvider) {
        view.detachBody();
        @Nullable ExpressionController newBodyController =
                newBodyControllerProvider.produceController();
        userLambda = UserLambda.create(
                userLambda.varName(),
                newBodyController != null ? newBodyController.getExpression() : null);
        view.attachBody(newBodyController != null ? newBodyController.getView() : null);
        updateDragActionSubscription();
        if (newBodyController != null) {
            newBodyController.setOnChangeCallback(this::handleBodyChange);
        }
        bodyController = newBodyController;
        onChangeCallback.onChange(() -> this);
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
            ScreenPoint screenPos = view.getBodyPos();
            ExpressionController controllerToDrag = bodyController;
            // This detaches the view from the UI, so it's safe to add the root view as a parent. It
            // also changes some class fields, so we need to grab them above.
            // TODO: Try to make things immutable to avoid this complexity.
            handleBodyChange(() -> null);
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
                    UserVariable.create(userLambda.varName()), view.getScreenPos());
        }
    }

    private class BodyDropTarget implements DropTarget {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (userLambda.body() == null && view.bodyIntersectsWith(dragController.getView())) {
                return view.getBodyViewDepth();
            } else {
                return DropTarget.NOT_HIT;
            }
        }
        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            view.handleBodyDragEnter();
        }
        @Override
        public void handleExit() {
            // Don't change our display unless we're actually accepting drops.
            if (userLambda.body() != null) {
                return;
            }
            view.handleBodyDragExit();
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            view.handleBodyDragExit();
            ExpressionController bodyController = expressionController.decommission();
            handleBodyChange(() -> bodyController);
        }
    }

    /**
     * Dropping a variable back should make it disappear instead of awkwardly stick around on the
     * canvas.
     */
    private class ParameterDropTarget implements DropTarget {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (dragController.getScreenExpression().getExpr() instanceof UserVariable &&
                    view.parameterIntersectsWith(dragController.getView())) {
                // Always have the lowest possible priority, since the only thing we're avoiding
                // here is putting
                return 0;
            } else {
                return DropTarget.NOT_HIT;
            }
        }
        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            // Do nothing
        }
        @Override
        public void handleExit() {
            // Do nothing
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            // Just throw the variable away.
            expressionController.decommission();
        }
    }
}
