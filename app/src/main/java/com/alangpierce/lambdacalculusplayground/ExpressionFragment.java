package com.alangpierce.lambdacalculusplayground;


import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment for an expression on the screen. This may be a self-contained expression, but can really
 * any lambda calculus expression, even one with free variables.
 */
public class ExpressionFragment extends Fragment {
    public ExpressionFragment() {
        // Required empty public constructor
    }

    private LinearLayout layoutView;

    private int xPos;
    private int yPos;
    private List<String> tokens;

    public static ExpressionFragment create(int xPos, int yPos, List<String> tokens) {
        Bundle args = new Bundle();
        args.putInt("xPos", xPos);
        args.putInt("yPos", yPos);
        args.putStringArrayList("tokens", new ArrayList<>(tokens));
        ExpressionFragment result = new ExpressionFragment();
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
            xPos = bundle.getInt("xPos");
            yPos = bundle.getInt("yPos");
            tokens = bundle.getStringArrayList("tokens");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("xPos", xPos);
        outState.putInt("yPos", yPos);
        outState.putStringArrayList("tokens", new ArrayList<>(tokens));
    }

    private TextView makeTextView(String text) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setTextSize(30);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setPadding(20, 20, 20, 20);
        return textView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layoutView = new LinearLayout(getActivity());
        // We should stretch to fit our content, but our position should be configurable.
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = xPos;
        layoutParams.topMargin = yPos;
        layoutView.setLayoutParams(layoutParams);

        layoutView.setBackgroundResource(R.drawable.expression);
        TextView lambdaView = makeTextView("λ");
        ViewDragger.attachToView(lambdaView, new ViewDragger.OnDragListener() {
            @Override
            public void onDrag(int dx, int dy) {
                setPosition(xPos + dx, yPos + dy);
            }
        });
        layoutView.addView(lambdaView);
        layoutView.addView(makeTextView("x"));
        layoutView.addView(makeTextView("x"));
        return layoutView;
    }

    /**
     * Update the stored position and redraw the view in the outer layout.
     */
    public void setPosition(int newXPos, int newYPos) {
        xPos = newXPos;
        yPos = newYPos;
        RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)layoutView.getLayoutParams();
        layoutParams.leftMargin = xPos;
        layoutParams.topMargin = yPos;
        layoutView.setLayoutParams(layoutParams);
    }
}
