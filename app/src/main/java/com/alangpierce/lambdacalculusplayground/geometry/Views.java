package com.alangpierce.lambdacalculusplayground.geometry;

import android.view.View;
import android.view.ViewParent;
import android.widget.RelativeLayout;

public class Views {
    public static ScreenPoint getScreenPos(View view) {
        final int location[] = { 0, 0 };
        view.getLocationOnScreen(location);
        // TODO: Make this check still work for views that are actually at (0, 0).
        if (location[0] == 0 && location[1] == 0) {
            throw new IllegalStateException("Cannot accurately compute the screen position for " +
                    "view " + view + " because it is not on the screen.");
        }
        return ScreenPoint.create(location[0], location[1]);
    }

    public static ScreenRect getBoundingBox(View view) {
        ScreenPoint topLeft = getScreenPos(view);
        return ScreenRect.create(
                topLeft, topLeft.plus(PointDifference.create(view.getWidth(), view.getHeight())));
    }

    public static boolean viewsIntersect(View view1, View view2) {
        try {
            return getBoundingBox(view1).intersectsWith(getBoundingBox(view2));
        } catch (IllegalStateException e) {
            // TODO: Handle this in a cleaner way. This happens when one of the views isn't on the
            // screen anymore.
            return false;
        }
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

    /**
     * Get the "deepest" view in the view hierarchy.
     *
     * This is a nice rough approximation for drop priority.
     */
    public static int viewDepth(View view) {
        ViewParent currentView = view.getParent();
        for (int i = 0; i < 100; i++) {
            if (currentView == null) {
                return i;
            }
            currentView = currentView.getParent();
        }
        return 100;
    }

    public static RelativeLayout.LayoutParams layoutParamsForRelativePos(Point relativePos) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = relativePos.getX();
        params.topMargin = relativePos.getY();
        return params;
    }

    public static void updateLayoutParamsToRelativePos(View view, Point relativePos) {
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)view.getLayoutParams();
        layoutParams.leftMargin = relativePos.getX();
        layoutParams.topMargin = relativePos.getY();
        view.setLayoutParams(layoutParams);
    }

    public static RelativeLayout.LayoutParams layoutParamsForScreenPos(
            View rootView, Point screenPos) {
        return layoutParamsForRelativePos(screenPos.minus(Views.getScreenPos(rootView).asPoint()));
    }
}
