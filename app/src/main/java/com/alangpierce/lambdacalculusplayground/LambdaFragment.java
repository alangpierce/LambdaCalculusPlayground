package com.alangpierce.lambdacalculusplayground;


import android.os.Bundle;
import android.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Fragment for a lambda expression on the screen.
 */
public class LambdaFragment extends Fragment {
    public LambdaFragment() {
        // Required empty public constructor
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
        LinearLayout view = new LinearLayout(getActivity());
        view.setBackgroundResource(R.drawable.lambda);
        TextView lambdaView = makeTextView("λ");
        ViewDragger.attachToView(lambdaView, view);
        view.addView(lambdaView);
        view.addView(makeTextView("x"));
        view.addView(makeTextView("x"));
        return view;
    }
}
