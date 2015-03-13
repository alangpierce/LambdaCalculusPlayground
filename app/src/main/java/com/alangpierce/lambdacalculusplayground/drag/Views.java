package com.alangpierce.lambdacalculusplayground.drag;

import android.view.View;

public class Views {
    public static Point getScreenPos(View view) {
        final int location[] = { 0, 0 };
        view.getLocationOnScreen(location);
        return Point.create(location[0], location[1]);
    }
}
