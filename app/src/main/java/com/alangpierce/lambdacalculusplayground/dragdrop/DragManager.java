package com.alangpierce.lambdacalculusplayground.dragdrop;

public interface DragManager {
    void registerDragSource(DragSource dragSource);
    void registerDropTarget(DropTarget dropTarget);
}
