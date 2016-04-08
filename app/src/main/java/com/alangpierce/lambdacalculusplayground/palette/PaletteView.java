package com.alangpierce.lambdacalculusplayground.palette;

import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;

public class PaletteView {
    private final DrawerLayout drawerRoot;
    private final ScrollView scrollView;
    private final LinearLayout linearLayout;

    public PaletteView(DrawerLayout drawerRoot, ScrollView scrollView,
            LinearLayout linearLayout) {
        this.drawerRoot = drawerRoot;
        this.scrollView = scrollView;
        this.linearLayout = linearLayout;
    }

    public static PaletteView render(DrawerLayout rootView) {
        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.lambda_palette_scroll_view);
        LinearLayout linearLayout =
                (LinearLayout) rootView.findViewById(R.id.lambda_palette_linear_layout);
        rootView.setScrimColor(Color.TRANSPARENT);
        return new PaletteView(rootView, scrollView, linearLayout);
    }

    public void addChild(LambdaView lambdaView) {
        LinearLayout nativeView = lambdaView.getNativeView();
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(nativeView.getLayoutParams());
        // Convert 10dp to pixels.
        int marginPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10f, drawerRoot.getResources().getDisplayMetrics());
        layoutParams.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
        layoutParams.gravity = Gravity.CENTER;
        nativeView.setLayoutParams(layoutParams);
        linearLayout.addView(nativeView);
    }

    public boolean intersectsWithView(View other) {
        // Only allow intersection if the drawer is actually open.
        return drawerRoot.isDrawerOpen(GravityCompat.END) && Views.viewsIntersect(scrollView, other);
    }

    public View getNativeView() {
        return scrollView;
    }

    public void handleDeleteDragEnter() {
        scrollView.setBackgroundColor(getColor(R.color.palette_delete));
    }

    public void handleDeleteDragExit() {
        scrollView.setBackgroundColor(getColor(R.color.palette));
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(drawerRoot.getContext(), resId);
    }
}
