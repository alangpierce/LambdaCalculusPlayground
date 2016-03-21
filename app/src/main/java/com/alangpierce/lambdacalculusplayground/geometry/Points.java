package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;

public class Points {
    public static DrawableAreaPoint screenPointToDrawableAreaPoint(
            ScreenPoint screenPoint, View rootView) {
        ScreenPoint rootViewPos = Views.getScreenPos(rootView);
        return DrawableAreaPoint.create(
                screenPoint.getX() - rootViewPos.getX(),
                screenPoint.getY() - rootViewPos.getY());
    }
}
