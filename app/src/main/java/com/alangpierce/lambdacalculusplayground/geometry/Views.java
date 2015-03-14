package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;

public class Views {
    public static Point getScreenPos(View view) {
        final int location[] = { 0, 0 };
        view.getLocationOnScreen(location);
        return Point.create(location[0], location[1]);
    }

    public static Rect getBoundingBox(View view) {
        Point topLeft = getScreenPos(view);
        return Rect.create(topLeft, topLeft.plus(Point.create(view.getWidth(), view.getHeight())));
    }
}
