package com.alangpierce.lambdacalculusplayground;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor.
    }

    private static final int INITIAL_DRAWER_OPEN_DELAY_MS = 500;

    private TopLevelExpressionState expressionState = new TopLevelExpressionStateImpl();
    private DrawerLayout drawerRoot;

    public static PlaygroundFragment create(TopLevelExpressionState initialState) {
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
        expressionState.hydrateFromBundle(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        expressionState.persistToBundle(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout abovePaletteRoot = (RelativeLayout)
                inflater.inflate(R.layout.fragment_playground, container, false);
        drawerRoot = (DrawerLayout) abovePaletteRoot.findViewById(R.id.drawer_root_view);
        RelativeLayout canvasView = (RelativeLayout) abovePaletteRoot.findViewById(R.id.canvas_view);

        PlaygroundComponent component = DaggerPlaygroundComponent.builder()
                .playgroundModule(
                        new PlaygroundModule(getActivity(), canvasView, abovePaletteRoot,
                                drawerRoot, expressionState))
                .build();
        TopLevelExpressionManager expressionManager = component.getTopLevelExpressionManager();
        expressionManager.renderInitialData();

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
        return abovePaletteRoot;
    }

    @Override
    public void onDestroyView() {
        drawerRoot = null;
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_playground, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_palette) {
            if (drawerRoot != null) {
                if (drawerRoot.isDrawerOpen(GravityCompat.END)) {
                    drawerRoot.closeDrawer(GravityCompat.END);
                } else {
                    drawerRoot.openDrawer(GravityCompat.END);
                }
            }
        } else if (item.getItemId() == R.id.action_view_demo_video) {
            // TODO: Show the video in the app itself instead of going to YouTube.
            startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.demo_video_url))));
        }

        return super.onOptionsItemSelected(item);
    }
}
