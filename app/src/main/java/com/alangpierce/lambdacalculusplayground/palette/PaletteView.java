package com.alangpierce.lambdacalculusplayground.palette;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PaletteView {
    private final View nativeView;

    public PaletteView(View nativeView) {
        this.nativeView = nativeView;
    }

    public static PaletteView render(Context context, RelativeLayout rootView) {
        View view = new TextView(context);
        view.setBackgroundColor(0xFFE6CEA3);
        // Just below top-level expressions.
        view.setElevation(9);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(500, 2000);
        layoutParams.topMargin = 0;
        layoutParams.leftMargin = 1675;
        rootView.addView(view, layoutParams);
        return new PaletteView(view);
    }

    public View getNativeView() {
        return nativeView;
    }

    public void handleDeleteDragEnter() {
        nativeView.setBackgroundColor(0xFFAA0000);
    }

    public void handleDeleteDragExit() {
        nativeView.setBackgroundColor(0xFFE6CEA3);
    }
}
