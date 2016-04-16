package com.alangpierce.lambdacalculusplayground.view;

import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.geometry.Views;

public class ExpressionViews {
    public static boolean rightEdgeIntersectsWith(
            ExpressionView expressionView, TopLevelExpressionView dragView) {
        LinearLayout nativeView = expressionView.getNativeView();
        LinearLayout dragNativeView = dragView.getNativeView();
        try {
            return !Views.isAncestor(nativeView, dragNativeView) &&
                    Views.getBoundingBox(nativeView).rightEdge()
                            .intersectsWith(Views.getBoundingBox(dragNativeView));
        } catch (IllegalStateException e) {
            // TODO: Handle this in a cleaner way. This happens when one of the views isn't on the
            // screen anymore.
            return false;
        }
    }
}
