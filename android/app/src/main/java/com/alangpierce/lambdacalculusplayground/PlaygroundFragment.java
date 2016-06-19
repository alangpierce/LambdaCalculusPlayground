package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Top-level fragment. Note that generally this class shouldn't have significant logic; any
 * interesting pieces should move to dagger-injected classes.
 */
public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor.
    }

    private ReactNativeManager reactNativeManager;

    public static PlaygroundFragment create() {
        Bundle args = new Bundle();
        PlaygroundFragment result = new PlaygroundFragment();
        result.setArguments(args);
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        RelativeLayout root = (RelativeLayout)
                inflater.inflate(R.layout.fragment_playground, container, false);
        RelativeLayout canvasRoot = (RelativeLayout) root.findViewById(R.id.canvas_root);
        reactNativeManager = new ReactNativeManagerImpl(canvasRoot, activity);
        reactNativeManager.init();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        reactNativeManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        reactNativeManager.onPause();
    }
}
