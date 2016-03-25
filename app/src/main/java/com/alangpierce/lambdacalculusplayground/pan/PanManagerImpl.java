package com.alangpierce.lambdacalculusplayground.pan;

import android.util.Log;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.drag.DragObservableGenerator;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.geometry.ScreenPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PanManagerImpl implements PanManager {
    private static final String TAG = "PanManagerImpl";

    private final RelativeLayout rootView;
    private final DragObservableGenerator dragObservableGenerator;

    private final List<PanListener> panListeners = Collections.synchronizedList(new ArrayList<>());

    // panOffset is the point in canvas coordinates that should be used as the origin of the
    // drawable area. In other words, it's the position value that should be added to all
    // expressions when displaying them.
    private PointDifference panOffset;

    // Keep track of the original screen position of the RelativeLayout as well as the original
    // panOffset value when the drag started. Note that we don't technically need both, but it makes
    // this easier to reason about.
    private ScreenPoint currentDragStartPoint = null;
    private PointDifference currentDragOriginalPanOffset = null;

    public PanManagerImpl(RelativeLayout rootView,
            DragObservableGenerator dragObservableGenerator) {
        this.rootView = rootView;
        this.dragObservableGenerator = dragObservableGenerator;
    }

    @Override
    public void init(PointDifference initialPanOffset) {
        panOffset = initialPanOffset;
        // We view the process as dragging the whole rootView, and the coordinates we receive are of
        // the rootView itself as if the user was dragging it around. So it always starts out at the
        // top-left of the drawable area (a little bit below (0, 0)).
        dragObservableGenerator.getDragObservable(rootView).subscribe(panEvents -> {
            panEvents.subscribe(event -> {
                switch (event.getAction()) {
                    case DOWN: {
                        currentDragStartPoint = event.getScreenPos();
                        currentDragOriginalPanOffset = panOffset;
                    }
                    case MOVE: {
                        PointDifference dragDelta =
                                event.getScreenPos().minus(currentDragStartPoint);
                        panOffset = currentDragOriginalPanOffset.plus(dragDelta);
                        break;
                    }
                    case UP: {
                        currentDragStartPoint = null;
                        currentDragOriginalPanOffset = null;
                        break;
                    }
                }
                for (PanListener panListener : panListeners) {
                    panListener.onPan();
                }
            });
        });
    }

    @Override
    public void registerPanListener(PanListener panListener) {
        panListeners.add(panListener);
    }

    @Override
    public void unregisterPanListener(PanListener panListener) {
        boolean wasModified = panListeners.remove(panListener);
        if (!wasModified) {
            Log.w(TAG, "Unregistered a listener that wasn't registered.");
        }
    }

    @Override
    public PointDifference getPanOffset() {
        return panOffset;
    }
}
