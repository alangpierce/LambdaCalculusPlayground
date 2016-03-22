package com.alangpierce.lambdacalculusplayground.view;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alangpierce.lambdacalculusplayground.R;
import com.melnykov.fab.FloatingActionButton;

import java.util.List;

public class ExpressionViewRendererImpl implements ExpressionViewRenderer {
    private final Context context;

    public ExpressionViewRendererImpl(Context context) {
        this.context = context;
    }

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
    public static class ExpressionLayout extends LinearLayout {
        public ExpressionLayout(Context context) {
            super(context);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }
    }

    @Override
    public LinearLayout makeLinearLayoutWithChildren(List<View> children) {
        LinearLayout result = new ExpressionLayout(context);
        for (View child : children) {
            result.addView(child);
        }
        return styleLayout(result);
    }

    @Override
    public TextView makeTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setPadding(20, 0, 20, 0);
        return textView;
    }

    @Override
    public LinearLayout styleLayout(final LinearLayout layout) {
        layout.setBackgroundColor(Color.WHITE);
        layout.setPadding(6, 3, 6, 3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(6, 3, 6, 3);
        layout.setLayoutParams(layoutParams);
        layout.setVerticalGravity(Gravity.CENTER_VERTICAL);
        layout.setElevation(10);
        return layout;
    }

    @Override
    public LinearLayout makeMissingBodyView() {
        LinearLayout layout = new LinearLayout(context);
        layout.setBackgroundColor(0x44FF0000);
        layout.setPadding(3, 3, 3, 3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(3, 3, 3, 3);
        layout.setLayoutParams(layoutParams);
        layout.setVerticalGravity(Gravity.CENTER_VERTICAL);
        layout.setElevation(10);
        layout.addView(makeTextView(" "));
        return layout;
    }

    public View makeExecuteButton() {
        FloatingActionButton button = new FloatingActionButton(context);
        button.setType(FloatingActionButton.TYPE_MINI);
        button.setImageResource(R.drawable.ic_av_play_arrow);
        button.setColorNormal(0xFF00AA00);
        button.setColorRipple(0xFF00BB00);
        button.setColorPressed(0xFF00CC00);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(80, 80);
        button.setLayoutParams(params);
        return button;
    }
}
