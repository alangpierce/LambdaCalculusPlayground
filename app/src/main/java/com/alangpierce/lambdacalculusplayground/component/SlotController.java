package com.alangpierce.lambdacalculusplayground.component;

import com.alangpierce.lambdacalculusplayground.CanvasManager;
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
    private final CanvasManager canvasManager;
    private final SlotView view;

    private final Subject<Observable<PointerMotionEvent>, Observable<PointerMotionEvent>>
            dragActionSubject = PublishSubject.create();
    private @Nullable Subscription dragActionSubscription;

    private SlotControllerParent parent;
    private @Nullable ExpressionController exprController;

    public SlotController(
            CanvasManager canvasManager,
            SlotView view,
            @Nullable ExpressionController exprController) {
        this.canvasManager = canvasManager;
        this.view = view;
        this.exprController = exprController;
    }

    public void setParent(SlotControllerParent parent) {
        this.parent = parent;
    }

    // Note that the expression produced might be null.
    public void handleChange(ExpressionControllerProvider newControllerProvider) {
        view.detach();
        @Nullable ExpressionController newController = newControllerProvider.produceController();
        parent.updateSlotExpression(newController != null ? newController.getExpression() : null);
        view.attach(newController != null ? newController.getView() : null);
        updateDragActionSubscription();
        if (newController != null) {
            newController.setOnChangeCallback(this::handleChange);
        }
        exprController = newController;
        parent.handleChange();
    }

    public void invalidateDefinitions() {
        if (exprController != null) {
            exprController.invalidateDefinitions();
        }
    }

    private void updateDragActionSubscription() {
        if (dragActionSubscription != null) {
            dragActionSubscription.unsubscribe();
            dragActionSubscription = null;
        }
        @Nullable Observable<? extends Observable<PointerMotionEvent>> observable =
                view.getObservable();
        if (observable != null) {
            dragActionSubscription = observable.subscribe(dragActionSubject);
        }
    }

    public DropTarget<?> getDropTarget() {
        return new SlotDropTarget();
    }

    public DragSource getDragSource() {
        // TODO: It's ugly to do this in a method with a "getter" name.
        updateDragActionSubscription();
        return new SlotDragSource();
    }

    private class SlotDragSource implements DragSource {
        @Override
        public Observable<? extends Observable<PointerMotionEvent>> getDragObservable() {
            return dragActionSubject;
        }
        @Override
        public TopLevelExpressionController handleStartDrag() {
            ScreenPoint screenPos = view.getPos();
            ExpressionController controllerToDrag = exprController;
            // This detaches the view from the UI, so it's safe to add the root view as a parent. It
            // also changes some class fields, so we need to grab them above.
            // TODO: Try to make things immutable to avoid this complexity.
            handleChange(() -> null);
            return canvasManager.sendExpressionToTopLevel(controllerToDrag, screenPos);
        }
    }

    private class SlotDropTarget implements DropTarget<TopLevelExpressionController> {
        @Override
        public int hitTest(TopLevelExpressionController dragController) {
            if (exprController == null && view.intersectsWith(dragController.getView())) {
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
            if (exprController != null) {
                return;
            }
            view.handleDragExit();
        }
        @Override
        public void handleDrop(TopLevelExpressionController expressionController) {
            view.handleDragExit();
            ExpressionController exprController = expressionController.decommission();
            handleChange(() -> exprController);
        }
        @Override
        public Class<TopLevelExpressionController> getDataClass() {
            return TopLevelExpressionController.class;
        }
    }
}
