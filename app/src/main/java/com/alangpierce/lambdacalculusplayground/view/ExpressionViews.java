package com.alangpierce.lambdacalculusplayground.view;

import android.graphics.Color;
import android.view.ViewGroup;
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

    public static void handleDragEnter(ExpressionView view) {
        view.getNativeView().setBackgroundColor(Color.GREEN);
    }

    public static void handleDragExit(ExpressionView view) {
        view.getNativeView().setBackgroundColor(Color.WHITE);
    }

    public static void detach(ExpressionView view) {
        LinearLayout nativeView = view.getNativeView();
        ((ViewGroup)nativeView.getParent()).removeView(nativeView);
    }
}
