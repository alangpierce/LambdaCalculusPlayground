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
}
