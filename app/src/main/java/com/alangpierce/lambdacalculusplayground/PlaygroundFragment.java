package com.alangpierce.lambdacalculusplayground;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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

    private static final int INITIAL_DRAWER_OPEN_DELAY_MS = 500;

    private TopLevelExpressionState expressionState = new TopLevelExpressionStateImpl();
    private DrawerLayout drawerRoot;

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
        drawerRoot = (DrawerLayout) inflater.inflate(R.layout.fragment_playground, container, false);
        RelativeLayout canvasView = (RelativeLayout) drawerRoot.findViewById(R.id.canvas_view);

        PlaygroundComponent component = DaggerPlaygroundComponent.builder()
                .playgroundModule(
                        new PlaygroundModule(getActivity(), canvasView, drawerRoot, expressionState))
                .build();
        TopLevelExpressionManager expressionManager = component.getTopLevelExpressionManager();
        expressionManager.renderInitialExpressions();

        // If this is the first time opening the app, open the drawer after a short delay. This
        // makes it so the palette animates in, which emphasizes that it's a drawer and makes sure
        // the user starts with it visible.
        if (savedInstanceState == null) {
            drawerRoot.postDelayed(() -> {
                if (drawerRoot != null) {
                    drawerRoot.openDrawer(GravityCompat.END);
                }
            }, INITIAL_DRAWER_OPEN_DELAY_MS);
        }

        return drawerRoot;
    }

    @Override
    public void onDestroyView() {
        drawerRoot = null;
        super.onDestroyView();
    }
}
