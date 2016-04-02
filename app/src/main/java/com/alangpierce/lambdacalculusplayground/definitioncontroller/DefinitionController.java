package com.alangpierce.lambdacalculusplayground.definitioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenDefinition;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragData;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragSource;
import com.alangpierce.lambdacalculusplayground.dragdrop.DropTarget;
import com.alangpierce.lambdacalculusplayground.pan.PanManager.PanListener;

import java.util.List;

import javax.annotation.Nullable;

public interface DefinitionController extends PanListener, DragData {
    List<DragSource> getDragSources();
    List<DropTarget<?>> getDropTargets();

    void setOnChangeCallback(OnDefinitionChangeCallback onChangeCallback);

    ScreenDefinition getScreenDefinition();

    interface OnDefinitionChangeCallback {
        void onChange(@Nullable DefinitionController newDefinitionController);
    }
}
