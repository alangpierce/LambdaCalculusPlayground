package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;
import android.view.ViewParent;
import android.widget.RelativeLayout;

public class Views {
    public static Point getScreenPos(View view) {
        final int location[] = { 0, 0 };
        view.getLocationOnScreen(location);
        // TODO: Make this check still work for views that are actually at (0, 0).
        if (location[0] == 0 && location[1] == 0) {
            throw new IllegalStateException("Cannot accurately compute the screen position for " +
                    "view " + view + " because it is not on the screen.");
        }
        return Point.create(location[0], location[1]);
    }

    public static Rect getBoundingBox(View view) {
        Point topLeft = getScreenPos(view);
        return Rect.create(topLeft, topLeft.plus(Point.create(view.getWidth(), view.getHeight())));
    }

    public static boolean viewsIntersect(View view1, View view2) {
        return getBoundingBox(view1).intersectsWith(getBoundingBox(view2));
    }

    public static boolean isAncestor(View possibleDescendant, View possibleAncestor) {
        if (possibleDescendant == possibleAncestor) {
            return true;
        }
        ViewParent view = possibleDescendant.getParent();
        // Give up after 100 iterations in case we have a cycle.
        for (int i = 0; view != null && i < 100; i++) {
            if (view == possibleAncestor) {
                return true;
            }
            view = view.getParent();
        }
        return false;
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
