package com.alangpierce.lambdacalculusplayground.geometry;

import com.alangpierce.lambdacalculusplayground.pan.PanManager;

import android.view.View;

public class PointConverterImpl implements PointConverter {
    private final View rootView;
    private final PanManager panManager;

    public PointConverterImpl(View rootView, PanManager panManager) {
        this.rootView = rootView;
        this.panManager = panManager;
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
        PointDifference panOffset = panManager.getPanOffset();
        return DrawableAreaPoint.create(
                canvasPoint.getX() + panOffset.getX(), canvasPoint.getY() + panOffset.getY());
    }

    @Override
    public CanvasPoint toCanvasPoint(ScreenPoint screenPoint) {
        return toCanvasPoint(toDrawableAreaPoint(screenPoint));
    }

    private CanvasPoint toCanvasPoint(DrawableAreaPoint drawableAreaPoint) {
        PointDifference panOffset = panManager.getPanOffset();
        return CanvasPoint.create(
                drawableAreaPoint.getX() - panOffset.getX(),
                drawableAreaPoint.getY() - panOffset.getY());
    }
}
