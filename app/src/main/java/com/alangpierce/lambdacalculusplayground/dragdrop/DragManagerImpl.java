package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import rx.Observable;

public class DragManagerImpl implements DragManager {
    private final List<DropTarget> dropTargets = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public void registerDragSource(DragSource dragSource) {
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragSource.getDragObservable();

        dragObservable.subscribe(dragEvents -> {
            AtomicReference<TopLevelExpressionController> controllerReference =
                    new AtomicReference<>();
            dragEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        TopLevelExpressionController controller = dragSource.handleStartDrag();
                        controllerReference.set(controller);
                        handleDown(controller);
                    }
                    case MOVE: {
                        TopLevelExpressionController controller = controllerReference.get();
                        if (controller != null) {
                            handleMove(controller, event);
                        }
                        break;
                    }
                    case UP: {
                        TopLevelExpressionController controller = controllerReference.get();
                        if (controller != null) {
                            handleUp(controller, event);
                        }
                        break;
                    }
                }
            });
        });
    }

    private void handleDown(TopLevelExpressionController controller) {
        TopLevelExpressionView view = controller.getView();
        // TODO: Make sure all callers properly set change callback and attach to the root view.
        view.startDrag();
    }

    private void handleMove(TopLevelExpressionController controller, PointerMotionEvent event) {
        TopLevelExpressionView view = controller.getView();
        view.setScreenPos(event.getScreenPos());
        DropTarget bestDropTarget = getBestDropTarget(controller);

        // TODO: Be smarter about this. We probably don't want to redo every drop target every time.
        // Note that this assumes that exit is idempotent, which restricts what we can do.
        for (DropTarget dropTarget : dropTargets) {
            if (dropTarget == bestDropTarget) {
                dropTarget.handleEnter(controller);
            } else {
                dropTarget.handleExit();
            }
        }
    }

    private void handleUp(TopLevelExpressionController controller, PointerMotionEvent event) {
        // Reset all drop targets to the un-highlighted state.
        for (DropTarget dropTarget : dropTargets) {
            dropTarget.handleExit();
        }

        TopLevelExpressionView view = controller.getView();
        view.endDrag();
        DropTarget bestDropTarget = getBestDropTarget(controller);
        if (bestDropTarget == null) {
            defaultHandleDrop(controller, event.getScreenPos());
        } else {
            bestDropTarget.handleDrop(controller);
        }
    }

    /**
     * Figure out which drop target is the best one for this situation. Returns null if no drop
     * targets match.
     */
    private @Nullable DropTarget getBestDropTarget(TopLevelExpressionController dragController) {
        DropTarget bestTarget = null;
        int bestPriority = DropTarget.NOT_HIT;

        for (DropTarget dropTarget : dropTargets) {
            int hitTestResult = dropTarget.hitTest(dragController);
            if (hitTestResult > bestPriority) {
                bestTarget = dropTarget;
                bestPriority = hitTestResult;
            }
        }
        return bestTarget;
    }

    private void defaultHandleDrop(
            TopLevelExpressionController expressionController, ScreenPoint screenPos) {
        expressionController.handlePositionChange(screenPos);
    }

    @Override
    public void registerDropTarget(DropTarget dropTarget) {
        dropTargets.add(dropTarget);
    }
}
