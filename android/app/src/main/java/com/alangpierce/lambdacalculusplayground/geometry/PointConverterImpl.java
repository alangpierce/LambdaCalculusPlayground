package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;

public class PointConverterImpl implements PointConverter {
    private final View rootView;

    public PointConverterImpl(View rootView) {
        this.rootView = rootView;
    }

    @Override
    public DrawableAreaPoint toDrawableAreaPoint(ScreenPoint screenPoint) {
        ScreenPoint rootViewPos = Views.getScreenPos(rootView);
        return DrawableAreaPoint.create(
                screenPoint.getX() - rootViewPos.getX(),
                screenPoint.getY() - rootViewPos.getY());
    }

    @Override
    public DrawableAreaPoint toDrawableAreaPoint(CanvasPoint canvasPoint) {
        PointDifference panOffset = PointDifference.create(0, 0);
        return DrawableAreaPoint.create(
                canvasPoint.getX() + panOffset.getX(), canvasPoint.getY() + panOffset.getY());
    }

    @Override
    public CanvasPoint toCanvasPoint(ScreenPoint screenPoint) {
        return toCanvasPoint(toDrawableAreaPoint(screenPoint));
    }

    @Override
    public CanvasPoint toCanvasPoint(DrawableAreaPoint drawableAreaPoint) {
        PointDifference panOffset = PointDifference.create(0, 0);
        return CanvasPoint.create(
                drawableAreaPoint.getX() - panOffset.getX(),
                drawableAreaPoint.getY() - panOffset.getY());
    }

    @Override
    public ScreenPoint toScreenPoint(DrawableAreaPoint drawableAreaPoint) {
        ScreenPoint rootViewPos = Views.getScreenPos(rootView);
        return ScreenPoint.create(
                drawableAreaPoint.getX() + rootViewPos.getX(),
                drawableAreaPoint.getY() + rootViewPos.getY());
    }
}
