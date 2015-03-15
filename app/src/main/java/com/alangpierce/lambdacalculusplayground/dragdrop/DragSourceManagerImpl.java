package com.alangpierce.lambdacalculusplayground.dragdrop;

import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;
import com.alangpierce.lambdacalculusplayground.view.TopLevelExpressionView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import autovalue.shaded.com.google.common.common.collect.Lists;
import rx.Observable;

public class DragSourceManagerImpl implements DragManager {
    private final List<DragSource> dragSources = Collections.synchronizedList(Lists.newArrayList());
    private final List<DropTarget> dropTargets = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public void registerDragSource(DragSource dragSource) {
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragSource.getDragObservable();

        dragObservable.subscribe(dragEvents -> {
            AtomicReference<TopLevelExpressionView> viewReference = new AtomicReference<>();
            dragEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        TopLevelExpressionController expressionController = dragSource.handleStartDrag();
                        TopLevelExpressionView view = expressionController.getView();
                        viewReference.set(view);
                        view.attachToRoot();
                        expressionController.setOnChangeCallback(
                                // onChange
                                // TODO: Register the callback correctly.
                                (newScreenExpression) -> {
                                });
                        view.startDrag();
                    }
                    case MOVE: {
                        TopLevelExpressionView view = viewReference.get();
                        if (view == null) {
                            break;
                        }
                        view.setScreenPos(event.getScreenPos());
                        break;
                    }
                    case UP: {
                        TopLevelExpressionView view = viewReference.get();
                        if (view == null) {
                            break;
                        }
                        view.endDrag();
                        break;
                    }
                }
            });
        });
    }
}
