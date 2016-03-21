package com.alangpierce.lambdacalculusplayground.pan;

import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.geometry.Point;

public class PanManagerImpl implements PanManager {
    private final RelativeLayout rootView;
    private final DragObservableGenerator dragObservableGenerator;

    // panOffset is the point in canvas coordinates that should be used as the origin of the
    // drawable area. In other words, it's the position value that should be added to all
    // expressions when displaying them.
    private Point panOffset = Point.create(0, 0);
    // currentDragOffset is difference between the pan offset and the drag origin for the current
    // drag operation. It stays constant for the lifetime of a drag operation.
    private Point currentDragOffset = null;

    public PanManagerImpl(RelativeLayout rootView,
            DragObservableGenerator dragObservableGenerator) {
        this.rootView = rootView;
        this.dragObservableGenerator = dragObservableGenerator;
    }

    @Override
    public void init() {
        // We view the process as dragging the whole rootView, and the coordinates we receive are of
        // the rootView itself as if the user was dragging it around. So it always starts out at the
        // top-left of the drawable area (a little bit below (0, 0)).
        dragObservableGenerator.getDragObservable(rootView).subscribe(panEvents -> {
            panEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        currentDragOffset = panOffset.minus(event.getScreenPos());
                    }
                    case MOVE: {
                        panOffset = event.getScreenPos().plus(currentDragOffset);
                        break;
                    }
                    case UP: {
                        currentDragOffset = null;
                        break;
                    }
                }
            });
        });
    }

    @Override
    public Point getPanOffset() {
        return panOffset;
    }
}
