package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.TopLevelExpressionManager;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionController.ExpressionControllerProvider;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

import javax.annotation.Nullable;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class SlotController {
    private final TopLevelExpressionManager topLevelExpressionManager;
    private final SlotView view;

    private final Subject<Observable<PointerMotionEvent>, Observable<PointerMotionEvent>>
            bodyDragActionSubject = PublishSubject.create();
    private @Nullable Subscription bodyDragActionSubscription;

    private SlotControllerParent parent;
    private @Nullable ExpressionController bodyController;

    public SlotController(
            TopLevelExpressionManager topLevelExpressionManager,
            SlotView view,
            @Nullable ExpressionController bodyController) {
        this.topLevelExpressionManager = topLevelExpressionManager;
        this.view = view;
        this.bodyController = bodyController;
    }

    public void setParent(SlotControllerParent parent) {
        this.parent = parent;
    }

    // Note that the returned body might be null.
    public void handleBodyChange(ExpressionControllerProvider newBodyControllerProvider) {
        view.detach();
        @Nullable ExpressionController newBodyController =
                newBodyControllerProvider.produceController();
        parent.updateSlotExpression(
                newBodyController != null ? newBodyController.getExpression() : null);
        view.attach(newBodyController != null ? newBodyController.getView() : null);
        updateDragActionSubscription();
        if (newBodyController != null) {
            newBodyController.setOnChangeCallback(this::handleBodyChange);
        }
        bodyController = newBodyController;
        parent.handleChange();
    }

    private void updateDragActionSubscription() {
        if (bodyDragActionSubscription != null) {
            bodyDragActionSubscription.unsubscribe();
            bodyDragActionSubscription = null;
        }
        @Nullable Observable<? extends Observable<PointerMotionEvent>> bodyObservable =
                view.getObservable();
        if (bodyObservable != null) {
            bodyDragActionSubscription = bodyObservable.subscribe(bodyDragActionSubject);
        }
    }

    public DropTarget<?> getDropTarget() {
        return new BodyDropTarget();
    }

    public DragSource getDragSource() {
        // TODO: It's ugly to do this in a method with a "getter" name.
        updateDragActionSubscription();
        return new BodyDragSource();
    }

    private class BodyDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return bodyDragActionSubject;
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            ScreenPoint screenPos = view.getPos();
            ExpressionController controllerToDrag = bodyController;
            // This detaches the view from the UI, so it's safe to add the root view as a parent. It
            // also changes some class fields, so we need to grab them above.
            // TODO: Try to make things immutable to avoid this complexity.
            handleBodyChange(() -> null);
            return topLevelExpressionManager.sendExpressionToTopLevel(controllerToDrag, screenPos);
        }
    }

    private class BodyDropTarget implements DropTarget<TopLevelExpressionController> {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (bodyController == null && view.intersectsWith(dragController.getView())) {
                return view.getViewDepth();
            } else {
                return DropTarget.NOT_HIT;
            }
        }
        @Override
        public void handleEnter(TopLevelExpressionController expressionController) {
            view.handleDragEnter();
        }
        @Override
        public void handleExit() {
            // Don't change our display unless we're actually accepting drops.
            if (bodyController != null) {
                return;
            }
            view.handleDragExit();
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            view.handleDragExit();
            ExpressionController bodyController = expressionController.decommission();
            handleBodyChange(() -> bodyController);
        }
        @Override
        public Class<TopLevelExpressionController> getDataClass() {
            return TopLevelExpressionController.class;
        }
    }
}
