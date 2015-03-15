package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.drag.PointerMotionEvent;

import java.util.Collections;
import java.util.List;

import autovalue.shaded.com.google.common.common.collect.Lists;
import rx.Observable;

public class DragSourceRegistryImpl implements DragSourceRegistry {
    private final DragObservableGenerator dragObservableGenerator;
    private final DropTargetRegistry dropTargetRegistry;

    private final List<DragSource> dragSources = Collections.synchronizedList(Lists.newArrayList());

    public DragSourceRegistryImpl(
            DragObservableGenerator dragObservableGenerator,
            DropTargetRegistry dropTargetRegistry) {
        this.dragObservableGenerator = dragObservableGenerator;
        this.dropTargetRegistry = dropTargetRegistry;
    }

    @Override
    public void registerDragSource(RelativeLayout rootView, DragSource dragSource) {
        Observable<? extends Observable<PointerMotionEvent>> dragObservable =
                dragSource.getDragObservable();
        dragObservable.subscribe(eventObservable -> {
            dragSource.handleStartDrag(rootView, eventObservable)
                    // TODO: Actually do something here.
                    .subscribe(event -> {});
        });
    }
}
