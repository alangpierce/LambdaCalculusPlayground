package com.alangpierce.lambdacalculusplayground;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.expressioncontroller.ExpressionControllerFactory;
import com.alangpierce.lambdacalculusplayground.expressioncontroller.TopLevelExpressionController;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import autovalue.shaded.com.google.common.common.collect.Maps;

public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor.
    }

    /*
     * We keep expressions with IDs so that we can delete and modify them later as necessary, but
     * the bundled format is just a list of ScreenExpressions.
     */
    private Map<Integer, ScreenExpression> expressions = Maps.newHashMap();
    private int maxId = 0;

    private PlaygroundComponent component;
    private RelativeLayout rootLayout;

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
        component = ((PlaygroundActivity)getActivity()).getComponent();
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
        List<ScreenExpression> screenExpressions =
                (List<ScreenExpression>)bundle.getSerializable("expressions");
        for (ScreenExpression expression : screenExpressions) {
            addExpression(expression);
        }
    }

    private void addExpression(ScreenExpression screenExpression) {
        int exprId = maxId + 1;
        expressions.put(exprId, screenExpression);
        maxId++;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("expressions", ImmutableList.copyOf(expressions.values()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootLayout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams rootLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(rootLayoutParams);

        ExpressionControllerFactory expressionControllerFactory =
                component.getExpressionControllerFactoryFactory().create(rootLayout);

        for (Entry<Integer, ScreenExpression> entry : expressions.entrySet()) {
            int exprId = entry.getKey();
            ScreenExpression screenExpression = entry.getValue();
            addTopLevelExpression(expressionControllerFactory, exprId, screenExpression);
        }
        rootLayout.setBackgroundResource(R.drawable.expression);
        return rootLayout;
    }

    private void addTopLevelExpression(
            ExpressionControllerFactory expressionControllerFactory,
            int exprId, ScreenExpression screenExpression) {
        TopLevelExpressionController controller =
                expressionControllerFactory.createTopLevelController(screenExpression);
        controller.setCallbacks(
                (newScreenExpression) -> expressions.put(exprId, newScreenExpression),
                rootLayout::removeView);
        RelativeLayout.LayoutParams expressionParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        expressionParams.leftMargin = screenExpression.x;
        expressionParams.topMargin = screenExpression.y;
        rootLayout.addView(controller.getView().getNativeView(), expressionParams);
    }
}
