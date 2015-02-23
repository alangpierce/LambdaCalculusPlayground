package com.alangpierce.lambdacalculusplayground;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rootLayout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams rootLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(rootLayoutParams);

        DragTracker dragTracker = new DragTrackerImpl();
        for (ScreenExpression screenExpression : expressions) {
            ExpressionViewGenerator viewGenerator =
                    new ExpressionViewGenerator(getActivity(), dragTracker);
            View expressionView = viewGenerator.makeTopLevelExpressionView(screenExpression.expr);
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
}
