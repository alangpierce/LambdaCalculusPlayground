package com.alangpierce.lambdacalculusplayground.dragdrop;

import java.util.Collections;
import java.util.List;

import autovalue.shaded.com.google.common.common.collect.Lists;
import rx.Observable;

public class DropTargetRegistryImpl implements DropTargetRegistry {
    private final List<DropTarget> dropTargets = Collections.synchronizedList(Lists.newArrayList());

    @Override
    public void handleDragOperation(Observable<DragPacket> dragPackets) {
    }

    @Override
    public void registerDropTarget(DropTarget dropTarget) {
        dropTargets.add(dropTarget);
    }
}
