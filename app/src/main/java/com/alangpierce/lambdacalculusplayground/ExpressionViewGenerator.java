package com.alangpierce.lambdacalculusplayground;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

public class ExpressionViewGenerator {
    private final Context context;

    public ExpressionViewGenerator(Context context) {
        this.context = context;
    }

    public LinearLayout makeTopLevelExpressionView(UserExpression expr) {
        return styleLayout(makeExpressionView(expr));
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

    private LinearLayout styleLayout(LinearLayout layout) {
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

    private View makeMissingBodyView() {
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

    private LinearLayout makeExpressionView(final UserExpression expr) {
        return expr.visit(new UserExpression.UserExpressionVisitor<LinearLayout>() {
            @Override
            public LinearLayout visit(UserLambda lambda) {
                LinearLayout expressionLayout = new LinearLayout(context);
                expressionLayout.addView(makeTextView("λ"));
                TextView varView = makeTextView(lambda.varName);
                expressionLayout.addView(varView);
                if (lambda.body != null) {
                    expressionLayout.addView(styleLayout(makeExpressionView(lambda.body)));
                } else {
                    expressionLayout.addView(makeMissingBodyView());
                }
                return expressionLayout;
            }
            @Override
            public LinearLayout visit(UserFuncCall funcCall) {
                LinearLayout expressionLayout = new LinearLayout(context);
                expressionLayout.addView(makeExpressionView(funcCall.func));
                expressionLayout.addView(styleLayout(makeExpressionView(funcCall.arg)));
                return expressionLayout;
            }
            @Override
            public LinearLayout visit(UserVariable variable) {
                LinearLayout expressionLayout = new LinearLayout(context);
                expressionLayout.addView(makeTextView(variable.varName));
                return expressionLayout;
            }
        });
    }
}
