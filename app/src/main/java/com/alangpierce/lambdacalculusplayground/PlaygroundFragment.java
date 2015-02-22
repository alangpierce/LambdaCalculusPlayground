package com.alangpierce.lambdacalculusplayground;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.alangpierce.lambdacalculusplayground.userexpression.UserFuncCall;
import com.alangpierce.lambdacalculusplayground.userexpression.UserLambda;
import com.alangpierce.lambdacalculusplayground.userexpression.UserVariable;

import java.io.Serializable;
import java.util.List;

public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor
    }

    private List<ScreenExpression> expressions;

    public static PlaygroundFragment create(List<ScreenExpression> expressions) {
        Bundle args = new Bundle();
        args.putSerializable("expressions", (Serializable) expressions);
        PlaygroundFragment result = new PlaygroundFragment();
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        }
        if (bundle != null) {
            loadFromBundle(bundle);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromBundle(Bundle bundle) {
        expressions = (List<ScreenExpression>)bundle.getSerializable("expressions");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("expressions", (Serializable)expressions);
    }


    private TextView makeTextView(String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setPadding(20, 0, 20, 0);
        return textView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rootLayout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams rootLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(rootLayoutParams);

        for (ScreenExpression screenExpression : expressions) {
            View expressionView = styleLayout(makeExpressionView(screenExpression.expr));
            RelativeLayout.LayoutParams expressionParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            expressionParams.leftMargin = screenExpression.x;
            expressionParams.topMargin = screenExpression.y;
            expressionView.setLayoutParams(expressionParams);
            rootLayout.addView(expressionView);
        }
        rootLayout.setBackgroundResource(R.drawable.expression);
        return rootLayout;
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
        LinearLayout layout = new LinearLayout(getActivity());
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
                LinearLayout expressionLayout = new LinearLayout(getActivity());
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
                LinearLayout expressionLayout = new LinearLayout(getActivity());
                expressionLayout.addView(makeExpressionView(funcCall.func));
                expressionLayout.addView(styleLayout(makeExpressionView(funcCall.arg)));
                return expressionLayout;
            }
            @Override
            public LinearLayout visit(UserVariable variable) {
                LinearLayout expressionLayout = new LinearLayout(getActivity());
                expressionLayout.addView(makeTextView(variable.varName));
                return expressionLayout;
            }
        });
    }
}
