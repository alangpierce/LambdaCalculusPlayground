package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;
import android.widget.RelativeLayout;

public class Views {
    public static Point getScreenPos(View view) {
        final int location[] = { Integer.MIN_VALUE, Integer.MIN_VALUE };
        view.getLocationOnScreen(location);
        if (location[0] == Integer.MIN_VALUE && location[1] == Integer.MIN_VALUE) {
            throw new IllegalStateException("Cannot accurately compute the screen position for " +
                    "view " + view + " because it is not on the screen.");
        }
        return Point.create(location[0], location[1]);
    }

    public static Rect getBoundingBox(View view) {
        Point topLeft = getScreenPos(view);
        return Rect.create(topLeft, topLeft.plus(Point.create(view.getWidth(), view.getHeight())));
    }

    public static RelativeLayout.LayoutParams layoutParamsForRelativePos(Point relativePos) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = relativePos.getX();
        params.topMargin = relativePos.getY();
        return params;
    }

    public static RelativeLayout.LayoutParams layoutParamsForScreenPos(
            View rootView, Point screenPos) {
        return layoutParamsForRelativePos(screenPos.minus(Views.getScreenPos(rootView)));
    }
}
