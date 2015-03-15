package com.alangpierce.lambdacalculusplayground.dragdrop;

import android.widget.RelativeLayout;

public interface DragSourceRegistry {
    void registerDragSource(RelativeLayout rootView, DragSource dragSource);
}
