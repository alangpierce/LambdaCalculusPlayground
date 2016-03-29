package com.alangpierce.lambdacalculusplayground.view;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.alangpierce.lambdacalculusplayground.compat.Compat;

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

    /**
     * Only consider touch events that are actually over the button. To do this, we call the
     * internal getSizeDimension method to get the button radius, then see if the event is within
     * that radius of the center of the view. This is particularly important for older
     * implementations where there is a significant amount of padding to allow for the shadow.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int diameter = Compat.getFabSizeDimension(this);
        int radius = diameter / 2;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        float dx = event.getX() - centerX;
        float dy = event.getY() - centerY;
        if (dx * dx + dy * dy <= radius * radius) {
            return super.dispatchTouchEvent(event);
        } else {
            return false;
        }
    }
}
