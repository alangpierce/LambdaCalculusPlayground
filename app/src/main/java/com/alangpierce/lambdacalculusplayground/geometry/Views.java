package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;
import android.widget.RelativeLayout;

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

    public static RelativeLayout.LayoutParams layoutParamsForScreenPosition(
            View rootView, Point screenPos) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        Point relativePos = screenPos.minus(Views.getScreenPos(rootView));
        params.leftMargin = relativePos.getX();
        params.topMargin = relativePos.getY();
        return params;
    }
}
