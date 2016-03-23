package com.alangpierce.lambdacalculusplayground.palette;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PaletteDrawerLayout extends DrawerLayout {
    public PaletteDrawerLayout(Context context) {
        super(context);
    }

    public PaletteDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaletteDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }
}
