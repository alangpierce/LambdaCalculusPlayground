package com.alangpierce.lambdacalculusplayground.view;

import android.content.Context;
import android.support.annotation.LayoutRes;
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
    private final LayoutInflater layoutInflater;

    public ExpressionViewRendererImpl(Context context, LayoutInflater layoutInflater) {
        this.context = context;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public LinearLayout makeExpressionViewWithChildren(List<View> children) {
        LinearLayout layout = (LinearLayout) inflateExpressionComponent(R.layout.expression_layout);
        for (View child : children) {
            layout.addView(child);
        }
        return layout;
    }

    @Override
    public TextView makeTextView(String text) {
        TextView result = (TextView) inflateExpressionComponent(R.layout.expression_text);
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
        ImageView result = (ImageView) inflateExpressionComponent(R.layout.bracket);
        int resId = BRACKET_DRAWABLE_BY_STRING.get(text);
        result.setImageDrawable(context.getResources().getDrawable(resId, null));
        return result;
    }

    @Override
    public View makeMissingBodyView() {
        return inflateExpressionComponent(R.layout.missing_body);
    }

    @Override
    public View makeExecuteButton() {
        return inflateAbsolute(R.layout.execute_button);
    }

    // Inflate an element that will generally be absolutely positioned (a child of the
    // RelativeLayout).
    private View inflateAbsolute(@LayoutRes int resId) {
        return layoutInflater.inflate(resId, new RelativeLayout(context), false);
    }

    // Inflate an element that will be inside an expression (a child of a LinearLayout).
    private View inflateExpressionComponent(@LayoutRes int resId) {
        return layoutInflater.inflate(resId, new LinearLayout(context), false);
    }
}
