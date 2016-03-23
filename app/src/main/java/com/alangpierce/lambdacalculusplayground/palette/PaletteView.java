package com.alangpierce.lambdacalculusplayground.palette;

import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.alangpierce.lambdacalculusplayground.R;
import com.alangpierce.lambdacalculusplayground.view.LambdaView;

public class PaletteView {
    private final ScrollView scrollView;
    private final LinearLayout linearLayout;

    public PaletteView(ScrollView scrollView, LinearLayout linearLayout) {
        this.scrollView = scrollView;
        this.linearLayout = linearLayout;
    }

    public static PaletteView render(DrawerLayout rootView) {
        ScrollView scrollView = (ScrollView) rootView.findViewById(R.id.palette_scroll_view);
        LinearLayout linearLayout =
                (LinearLayout) rootView.findViewById(R.id.palette_linear_layout);
        rootView.setScrimColor(Color.TRANSPARENT);
        return new PaletteView(scrollView, linearLayout);
    }

    public void addChild(LambdaView lambdaView) {
        LinearLayout nativeView = lambdaView.getNativeView();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) nativeView.getLayoutParams();
        // TODO: Use dps here instead of pixels.
        layoutParams.setMargins(30, 30, 30, 30);
        layoutParams.gravity = Gravity.CENTER;
        nativeView.setLayoutParams(layoutParams);
        linearLayout.addView(nativeView);
    }

    public View getNativeView() {
        return scrollView;
    }

    public void handleDeleteDragEnter() {
        scrollView.setBackgroundColor(0xFFAA0000);
    }

    public void handleDeleteDragExit() {
        scrollView.setBackgroundColor(0xFFE6CEA3);
    }
}
