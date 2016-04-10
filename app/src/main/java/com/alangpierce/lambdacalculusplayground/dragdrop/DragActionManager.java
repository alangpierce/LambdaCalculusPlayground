package com.alangpierce.lambdacalculusplayground.dragdrop;

public interface DragActionManager {
    void initDropTargets(DragManager dragManager);
    void handleDragDown();
    void handleDragUp();
}
