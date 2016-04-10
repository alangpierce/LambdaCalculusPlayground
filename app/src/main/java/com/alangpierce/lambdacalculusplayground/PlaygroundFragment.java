package com.alangpierce.lambdacalculusplayground;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.dragdrop.DragActionManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.palette.PaletteDrawerManager;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Top-level fragment. Note that generally this class shouldn't have significant logic; any
 * interesting pieces should move to dagger-injected classes.
 */
public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor.
    }

    private AppState appState = new AppStateImpl();

    @Bind(R.id.playground_toolbar) Toolbar toolbar;

    @Inject TopLevelExpressionManager expressionManager;
    @Inject ExpressionCreator expressionCreator;
    @Inject PaletteDrawerManager paletteDrawerManager;
    @Inject DragManager dragManager;
    @Inject DragActionManager dragActionManager;

    public static PlaygroundFragment create(AppState initialState) {
        Bundle args = new Bundle();
        initialState.persistToBundle(args);
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
        setHasOptionsMenu(true);
    }

    @SuppressWarnings("unchecked")
    private void loadFromBundle(Bundle bundle) {
        appState.hydrateFromBundle(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        appState.persistToBundle(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        PlaygroundActivity activity = (PlaygroundActivity) getActivity();

        RelativeLayout root = (RelativeLayout)
                inflater.inflate(R.layout.fragment_playground, container, false);
        ButterKnife.bind(this, root);

        activity.setSupportActionBar(toolbar);

        PlaygroundComponent component = DaggerPlaygroundComponent.builder()
                .playgroundModule(PlaygroundModule.create(activity, appState, root))
                .build();
        component.injectPlaygroundFragment(this);

        expressionManager.renderInitialData();
        dragActionManager.initDropTargets(dragManager);

        boolean isFirstTime = savedInstanceState == null;
        paletteDrawerManager.onCreateView(isFirstTime);
        return root;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        paletteDrawerManager.onViewStateRestored();
        super.onViewStateRestored(savedInstanceState);
    }

    @OnClick(R.id.create_lambda_button)
    public void createLambdaClick() {
        expressionCreator.promptCreateLambda();
    }

    @OnClick(R.id.create_definition_button)
    public void createDefinitionClick() {
        expressionCreator.promptCreateDefinition();
    }

    @Override
    public void onDestroyView() {
        paletteDrawerManager.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playground, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_palette) {
            paletteDrawerManager.toggleLambdaPalette();
        } else if (item.getItemId() == R.id.action_define) {
            paletteDrawerManager.toggleDefinitionPalette();
        } else if (item.getItemId() == R.id.action_view_demo_video) {
            // TODO: Show the video in the app itself instead of going to YouTube.
            startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.demo_video_url))));
        }
        return super.onOptionsItemSelected(item);
    }
}
