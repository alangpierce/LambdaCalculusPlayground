package com.alangpierce.lambdacalculusplayground.dragdrop;

import rx.Observable;

public interface DropTargetRegistry {
    void handleDragOperation(Observable<DragPacket> dragPackets);
    void registerDropTarget(DropTarget dropTarget);
}
