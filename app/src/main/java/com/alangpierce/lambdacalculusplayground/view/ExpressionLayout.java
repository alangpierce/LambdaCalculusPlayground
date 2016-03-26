package com.alangpierce.lambdacalculusplayground.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * An expression is *almost* just a horizontal LinearLayout, so we reuse that as much as we can.
 * However, with expressions, we always want them to take on their full size, so we ignore any
 * incoming MeasureSpecs and just use UNSPECIFIED (i.e. "be as big as you need to be"). If we
 * didn't do this, RelativeLayout would try to shrink us when we're part of the way off-screen,
 * which results in a weird visual bug.
 *
 * TODO: Consider passing around this class instead of LinearLayout everywhere. In general,
 * though, this class is a bit of a hacky bug fix rather than an intentional attempt at
 * introducing something like type safety here.
 */
public class ExpressionLayout extends LinearLayout {
    public ExpressionLayout(Context context) {
        super(context);
    }
    public ExpressionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ExpressionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }
}
