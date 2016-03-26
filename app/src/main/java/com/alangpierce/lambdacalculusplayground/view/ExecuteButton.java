package com.alangpierce.lambdacalculusplayground.view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Just like with ExpressionLayout, we hack the FloatingActionButton class to ignore the measure
 * spec passed from the parent RelativeLayout. If we didn't do this, the button would get smaller
 * when the button is near the right or bottom of the screen, which looks bad.
 */
public class ExecuteButton extends FloatingActionButton {
    public ExecuteButton(Context context) {
        super(context);
    }
    public ExecuteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExecuteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }
}
