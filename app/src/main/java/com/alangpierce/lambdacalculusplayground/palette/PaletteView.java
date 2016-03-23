package com.alangpierce.lambdacalculusplayground.palette;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.alangpierce.lambdacalculusplayground.view.LambdaView;

public class PaletteView {
    private final ScrollView scrollView;
    private final LinearLayout linearLayout;

    public PaletteView(ScrollView scrollView, LinearLayout linearLayout) {
        this.scrollView = scrollView;
        this.linearLayout = linearLayout;
    }

    public static class PaletteScrollView extends ScrollView {
        public PaletteScrollView(Context context) {
            super(context);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            // This method is called when doing a touch operation on a child view. Generally, the
            // rule is that the ScrollView takes over once a touch event has exceeded the touch
            // slop. However, in our case, we know that we never want to scroll unless the touch
            // event is on the ScrollView directly, so just always return false here.
            // TODO: Ideally an up/down gesture would scroll up/down even if it starts on a palette
            // lambda. We could implement this method in a smarter way to allow that.
            return false;
        }
    }

    public static PaletteView render(Context context, RelativeLayout rootView) {
        ScrollView scrollView = new PaletteScrollView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);

        scrollView.setBackgroundColor(0xFFE6CEA3);
        // Just below top-level expressions.
        scrollView.setElevation(9);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rootView.addView(scrollView, layoutParams);
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
