package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.geometry.Point;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import autovalue.shaded.com.google.common.common.collect.Lists;
import rx.Observable;
import rx.Subscription;

public class DragManagerImpl implements DragManager {
    private final List<DropTarget> dropTargets = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public void registerDragSource(DragSource dragSource) {
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragSource.getDragObservable();

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        subscription.set(dragObservable.subscribe(dragEvents -> {
            AtomicReference<TopLevelExpressionController> controllerReference =
                    new AtomicReference<>();
            dragEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        TopLevelExpressionController controller =
                                dragSource.handleStartDrag(subscription.get());
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
        }));
    }

    private void handleDown(TopLevelExpressionController controller) {
        TopLevelExpressionView view = controller.getView();
        // TODO: Make sure all callers properly set change callback and attach to the root view.
        view.startDrag();
    }

    private void handleMove(TopLevelExpressionController controller, PointerMotionEvent event) {
        TopLevelExpressionView view = controller.getView();
        view.setScreenPos(event.getScreenPos());
    }

    private void handleUp(TopLevelExpressionController controller, PointerMotionEvent event) {
        TopLevelExpressionView view = controller.getView();
        view.endDrag();
        defaultHandleDrop(controller, event.getScreenPos());
    }

    private void defaultHandleDrop(
            TopLevelExpressionController expressionController, Point screenPos) {
        expressionController.handlePositionChange(screenPos);
    }

    @Override
    public void registerDropTarget(DropTarget dropTarget) {
        dropTargets.add(dropTarget);
    }
}
