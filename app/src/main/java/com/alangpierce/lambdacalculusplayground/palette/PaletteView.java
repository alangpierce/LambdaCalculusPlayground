package com.alangpierce.lambdacalculusplayground.palette;

import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class PaletteView {
    private final DrawerLayout drawerRoot;
    private final ScrollView drawerView;
    private final LinearLayout linearLayout;

    public PaletteView(DrawerLayout drawerRoot, ScrollView drawerView, LinearLayout linearLayout) {
        this.drawerRoot = drawerRoot;
        this.drawerView = drawerView;
        this.linearLayout = linearLayout;
    }

    public void onCreateView() {
        drawerRoot.setScrimColor(Color.TRANSPARENT);
    }

    public void addChild(View nativeView) {
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

    public View getNativeView() {
        return drawerView;
    }
}
