package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import rx.Observable;

public class DragManagerImpl implements DragManager {
    private final List<DropTarget<?>> dropTargets =
            Collections.synchronizedList(Lists.newArrayList());

    private final DragActionManager dragActionManager;

    public DragManagerImpl(
            DragActionManager dragActionManager) {
        this.dragActionManager = dragActionManager;
    }

    @Override
    public void registerDragSource(DragSource dragSource) {
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragSource.getDragObservable();

        dragObservable.subscribe(dragEvents -> {
            AtomicReference<DragData> dragDataReference = new AtomicReference<>();
            dragEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        DragData dragData = dragSource.handleStartDrag();
                        dragDataReference.set(dragData);
                        handleDown(dragData);
                    }
                    case MOVE: {
                        DragData dragData = dragDataReference.get();
                        if (dragData != null) {
                            handleMove(dragData, event);
                        }
                        break;
                    }
                    case UP: {
                        DragData dragData = dragDataReference.get();
                        if (dragData != null) {
                            handleUp(dragData, event);
                        }
                        break;
                    }
                }
            });
        });
    }

    private void handleDown(DragData dragData) {
        dragActionManager.handleDragDown();
        dragData.startDrag();
    }

    private <T extends DragData> void handleMove(T dragData, PointerMotionEvent event) {
        dragData.setScreenPos(event.getScreenPos());
        DropTarget<T> bestDropTarget = getBestDropTarget(dragData);

        // TODO: Be smarter about this. We probably don't want to redo every drop target every time.
        // Note that this assumes that exit is idempotent, which restricts what we can do (e.g.
        // animations).
        for (DropTarget<?> dropTarget : dropTargets) {
            if (dropTarget != bestDropTarget) {
                dropTarget.handleExit();
            }
        }
        if (bestDropTarget != null) {
            bestDropTarget.handleEnter(dragData);
        }
    }

    private <T extends DragData> void handleUp(T dragData, PointerMotionEvent event) {
        // Reset all drop targets to the un-highlighted state.
        for (DropTarget<?> dropTarget : dropTargets) {
            dropTarget.handleExit();
        }

        dragData.endDrag();
        DropTarget<T> bestDropTarget = getBestDropTarget(dragData);
        if (bestDropTarget == null) {
            dragData.handlePositionChange(event.getScreenPos());
        } else {
            bestDropTarget.handleDrop(dragData);
        }
        dragActionManager.handleDragUp();
    }

    /**
     * Figure out which drop target is the best one for this situation. Returns null if no drop
     * targets match.
     */
    private @Nullable <T extends DragData> DropTarget<T> getBestDropTarget(T dragData) {
        DropTarget<T> bestTarget = null;
        int bestPriority = DropTarget.NOT_HIT;

        for (DropTarget<?> dropTarget : dropTargets) {
            int hitTestResult = hitTest(dropTarget, dragData);
            if (hitTestResult > bestPriority) {
                // TODO: Consider refactoring this so the compiler can prove it. Still, it's
                // guaranteed safe because hitTest will only return a value greater than NOT_HIT if
                // the class is correct.
                //noinspection unchecked
                bestTarget = (DropTarget<T>) dropTarget;
                bestPriority = hitTestResult;
            }
        }
        return bestTarget;
    }

    private <T extends DragData> int hitTest(DropTarget<T> dropTarget, DragData dragData) {
        Class<T> dataClass = dropTarget.getDataClass();
        if (dataClass.isInstance(dragData)) {
            return dropTarget.hitTest(dataClass.cast(dragData));
        } else {
            return DropTarget.NOT_HIT;
        }
    }

    @Override
    public void registerDropTarget(DropTarget<?> dropTarget) {
        dropTargets.add(dropTarget);
    }
}
