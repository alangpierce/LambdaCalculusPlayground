package com.alangpierce.lambdacalculusplayground.view;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alangpierce.lambdacalculusplayground.R;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

public class ExpressionViewRendererImpl implements ExpressionViewRenderer {
    private final Context context;
    private final RelativeLayout rootView;
    private final LayoutInflater layoutInflater;

    public ExpressionViewRendererImpl(Context context, RelativeLayout rootView,
                                      LayoutInflater layoutInflater) {
        this.context = context;
        this.rootView = rootView;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public LinearLayout makeLinearLayoutWithChildren(List<View> children) {
        LinearLayout layout = new ExpressionLayout(context);
        for (View child : children) {
            layout.addView(child);
        }
        layout.setBackgroundColor(getColor(R.color.expression_background));
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
    public TextView makeTextView(String text) {
        TextView result = (TextView) inflate(R.layout.expression_text);
        result.setText(text);
        return result;
    }

    Map<String, Integer> BRACKET_DRAWABLE_BY_STRING = ImmutableMap.of(
            "(", R.drawable.drawable_left_paren,
            ")", R.drawable.drawable_right_paren,
            "[", R.drawable.drawable_left_bracket,
            "]", R.drawable.drawable_right_bracket);

    @Override
    public View makeBracketView(String text) {
        ImageView result = (ImageView) inflate(R.layout.bracket);
        int resId = BRACKET_DRAWABLE_BY_STRING.get(text);
        result.setImageDrawable(context.getResources().getDrawable(resId, null));
        return result;
    }

    @Override
    public View makeMissingBodyView() {
        return inflate(R.layout.missing_body);
    }

    @Override
    public View makeExecuteButton() {
        return inflate(R.layout.execute_button);
    }

    // We always want to inflate the view in a way where we inform the inflater about the root (so
    // that the generated view has the right layout params), but we never want to actually attach to
    // that root.
    private View inflate(@LayoutRes int resId) {
        return layoutInflater.inflate(resId, rootView, false);
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(context, resId);
    }
}
