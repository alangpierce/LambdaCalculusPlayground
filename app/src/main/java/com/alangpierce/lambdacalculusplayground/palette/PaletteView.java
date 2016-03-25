package com.alangpierce.lambdacalculusplayground.palette;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.geometry.Views;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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
        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.palette_scroll_view);
        LinearLayout linearLayout =
                (LinearLayout) rootView.findViewById(R.id.palette_linear_layout);
        rootView.setScrimColor(Color.TRANSPARENT);
        return new PaletteView(rootView, scrollView, linearLayout);
    }

    public void addChild(LambdaView lambdaView) {
        LinearLayout nativeView = lambdaView.getNativeView();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(nativeView.getLayoutParams());
        // TODO: Use dps here instead of pixels.
        layoutParams.setMargins(30, 30, 30, 30);
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

    private int getColor(int resId) {
        return ContextCompat.getColor(drawerRoot.getContext(), resId);
    }
}
