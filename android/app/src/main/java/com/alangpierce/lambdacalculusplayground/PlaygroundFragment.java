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

import com.alangpierce.lambdacalculusplayground.definition.DefinitionManager;
import com.alangpierce.lambdacalculusplayground.dragdrop.DragManager;
import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManager;

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

    @Inject CanvasManager canvasManager;
    @Inject ExpressionCreator expressionCreator;
    @Inject DragManager dragManager;
    @Inject DefinitionManager definitionManager;
    @Inject ReactNativeManager reactNativeManager;

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
        MainActivity activity = (MainActivity) getActivity();

        RelativeLayout root = (RelativeLayout)
                inflater.inflate(R.layout.fragment_playground, container, false);
        ButterKnife.bind(this, root);

        PlaygroundComponent component = DaggerPlaygroundComponent.builder()
                .playgroundModule(PlaygroundModule.create(activity, appState, root))
                .build();
        component.injectPlaygroundFragment(this);
        reactNativeManager.init();

        // Note that we need to invalidate the definitions before placing the expressions and
        // definitions, so that errors will be reported correctly.
        definitionManager.invalidateDefinitions();
        canvasManager.renderInitialData();

        boolean isFirstTime = savedInstanceState == null;
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
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_enable_numbers);
        item.setChecked(appState.isAutomaticNumbersEnabled());
        super.onPrepareOptionsMenu(menu);
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
        if (item.getItemId() == R.id.action_lambda_palette) {
//            paletteDrawerManager.toggleLambdaPalette();
            reactNativeManager.toggleLambdaPalette();
            return true;
        } else if (item.getItemId() == R.id.action_definition_palette) {
//            paletteDrawerManager.toggleDefinitionPalette();
            reactNativeManager.toggleDefinitionPalette();
            return true;
        } else if (item.getItemId() == R.id.action_delete_definition) {
            expressionCreator.promptDeleteDefinition();
            return true;
        } else if (item.getItemId() == R.id.action_enable_numbers) {
            boolean newIsChecked = !item.isChecked();
            item.setChecked(newIsChecked);
            appState.setEnableAutomaticNumbers(newIsChecked);
            canvasManager.handleAutomaticNumbersChanged();
            return true;
        } else if (item.getItemId() == R.id.action_view_demo_video) {
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
