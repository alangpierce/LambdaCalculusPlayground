package com.alangpierce.lambdacalculusplayground.definitioncontroller;

import com.alangpierce.lambdacalculusplayground.ScreenDefinition;
import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointConverter;
import com.alangpierce.lambdacalculusplayground.pan.PanManager.PanListener;
import com.alangpierce.lambdacalculusplayground.view.DefinitionView;

public class DefinitionController implements PanListener {
    private final PointConverter pointConverter;

    private final ScreenDefinition screenDefinition;
    private final DefinitionView view;

    public DefinitionController(
            PointConverter pointConverter,
            ScreenDefinition screenDefinition,
            DefinitionView view) {
        this.pointConverter = pointConverter;
        this.screenDefinition = screenDefinition;
        this.view = view;
    }

    @Override
    public void onPan() {
        DrawableAreaPoint drawableAreaPoint =
                pointConverter.toDrawableAreaPoint(screenDefinition.canvasPos());
        view.setCanvasPos(drawableAreaPoint);
    }
}
