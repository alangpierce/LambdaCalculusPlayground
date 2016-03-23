package com.alangpierce.lambdacalculusplayground.palette;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class PaletteScrollView extends ScrollView {
    public PaletteScrollView(Context context) {
        super(context);
    }

    public PaletteScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaletteScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
