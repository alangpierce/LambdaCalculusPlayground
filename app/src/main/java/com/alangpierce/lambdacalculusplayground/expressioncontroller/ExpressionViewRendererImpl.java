package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

public class ExpressionViewRendererImpl implements ExpressionViewRenderer {
    private final Context context;

    public ExpressionViewRendererImpl(Context context) {
        this.context = context;
    }

    @Override
    public LinearLayout makeVariableView(String varName) {
        return makeLinearLayoutWithChildren(ImmutableList.of(makeTextView(varName)));
    }

    @Override
    public LinearLayout makeLambdaView(String varName, @Nullable LinearLayout body) {
        return makeLinearLayoutWithChildren(ImmutableList.of(
                makeTextView("λ"),
                makeTextView(varName),
                body != null ? body : makeMissingBodyView()
        ));
    }

    @Override
    public LinearLayout makeFuncCallView(LinearLayout func, LinearLayout arg) {
        return makeLinearLayoutWithChildren(ImmutableList.of(func, arg));
    }

    private LinearLayout makeLinearLayoutWithChildren(List<View> children) {
        LinearLayout result = new LinearLayout(context);
        for (View child : children) {
            result.addView(child);
        }
        return styleLayout(result);
    }

    private TextView makeTextView(String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setPadding(20, 0, 20, 0);
        return textView;
    }

    private LinearLayout styleLayout(final LinearLayout layout) {
        layout.setBackgroundColor(Color.WHITE);
        layout.setPadding(3, 3, 3, 3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(3, 3, 3, 3);
        layout.setLayoutParams(layoutParams);
        layout.setVerticalGravity(Gravity.CENTER_VERTICAL);
        layout.setElevation(10);
        return layout;
    }

    private LinearLayout makeMissingBodyView() {
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
}
