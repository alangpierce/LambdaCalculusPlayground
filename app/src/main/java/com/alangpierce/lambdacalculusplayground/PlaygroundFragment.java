package com.alangpierce.lambdacalculusplayground;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alangpierce.lambdacalculusplayground.geometry.DrawableAreaPoint;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlaygroundFragment extends Fragment {
    public PlaygroundFragment() {
        // Required empty public constructor.
    }

    private static final int INITIAL_DRAWER_OPEN_DELAY_MS = 500;

    @Bind(R.id.above_palette_root_view) RelativeLayout abovePaletteRoot;
    @Bind(R.id.drawer_root_view) DrawerLayout drawerRoot;
    @Bind(R.id.canvas_view) RelativeLayout canvasView;
    @Bind(R.id.fab_container) View fabContainer;
    @Bind(R.id.palette_scroll_view) View drawerView;

    private TopLevelExpressionState expressionState = new TopLevelExpressionStateImpl();
    TopLevelExpressionManager expressionManager;

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
        RelativeLayout root = (RelativeLayout)
                inflater.inflate(R.layout.fragment_playground, container, false);
        ButterKnife.bind(this, root);

        drawerRoot.addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float offset = drawerView.getWidth() * slideOffset;
                fabContainer.setTranslationX(-offset);
            }
        });

        PlaygroundComponent component = DaggerPlaygroundComponent.builder()
                .playgroundModule(
                        new PlaygroundModule(getActivity(), canvasView, abovePaletteRoot,
                                drawerRoot, expressionState))
                .build();
        expressionManager = component.getTopLevelExpressionManager();
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
        return root;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        // Any drawer changes set the translation, but
        if (drawerRoot.isDrawerOpen(drawerView)) {
            drawerView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            fabContainer.setTranslationX(-drawerView.getMeasuredWidth());
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @OnClick(R.id.create_lambda_button)
    public void createLambdaClick() {
        Toast.makeText(getActivity(), "TODO", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.create_definition_button)
    public void createDefinitionClick() {
        showNewDefinitionDialog();
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
        } else if (item.getItemId() == R.id.action_define) {
            showNewDefinitionDialog();
        } else if (item.getItemId() == R.id.action_view_demo_video) {
            // TODO: Show the video in the app itself instead of going to YouTube.
            startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.demo_video_url))));
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNewDefinitionDialog() {
        View inputView =
                getActivity().getLayoutInflater().inflate(R.layout.definition_name_dialog, null);
        EditText nameEditText = (EditText) inputView.findViewById(R.id.definition_name);
        AlertDialog alertDialog = new Builder(getActivity())
                .setTitle(R.string.create_definition)
                .setView(inputView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    addDefinitionWithName(nameEditText.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
    }

    private void addDefinitionWithName(String defName) {
        // Create the view at (50dp, 50dp).
        int shiftPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 50f, getResources().getDisplayMetrics());
        boolean alreadyOnCanvas = expressionManager.placeDefinition(
                defName, DrawableAreaPoint.create(shiftPixels, shiftPixels));
        if (alreadyOnCanvas) {
            Toast.makeText(getActivity(),
                    "Showing existing definition.", Toast.LENGTH_SHORT).show();
        }
    }
}
