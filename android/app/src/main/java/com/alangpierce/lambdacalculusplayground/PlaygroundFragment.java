package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManager;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Top-level fragment. Note that generally this class shouldn't have significant logic; any
 * interesting pieces should move to dagger-injected classes.
 */
public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor.
    }

    @Inject CanvasManager canvasManager;
    @Inject ExpressionCreator expressionCreator;
    @Inject DragManager dragManager;
    @Inject ReactNativeManager reactNativeManager;

    public static PlaygroundFragment create() {
        Bundle args = new Bundle();
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();

        RelativeLayout root = (RelativeLayout)
                inflater.inflate(R.layout.fragment_playground, container, false);
        ButterKnife.bind(this, root);

        PlaygroundComponent component = DaggerPlaygroundComponent.builder()
                .playgroundModule(PlaygroundModule.create(activity, root))
                .build();
        component.injectPlaygroundFragment(this);
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

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playground, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_view_demo_video) {
            reactNativeManager.viewDemoVideo();
            return true;
        } else if (item.getItemId() == R.id.action_show_dev_options) {
            reactNativeManager.showDevOptionsDialog();
            return true;
        } else if (item.getItemId() == R.id.action_refresh_js) {
            reactNativeManager.reloadJs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
