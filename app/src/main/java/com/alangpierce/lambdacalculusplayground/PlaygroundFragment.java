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
        // Required empty public constructor.
    }

    private TopLevelExpressionState expressionState = new TopLevelExpressionStateImpl();

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
        expressionState.hydrateFromBundle(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        expressionState.persistToBundle(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rootLayout = new RelativeLayout(getActivity());
        RelativeLayout.LayoutParams rootLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        rootLayout.setLayoutParams(rootLayoutParams);

        PlaygroundComponent component = Dagger_PlaygroundComponent.builder()
                .playgroundModule(new PlaygroundModule(getActivity(), rootLayout, expressionState))
                .build();
        TopLevelExpressionManager expressionManager = component.getTopLevelExpressionManager();
        expressionManager.renderInitialExpressions();

        rootLayout.setBackgroundResource(R.drawable.expression);
        return rootLayout;
    }
}
