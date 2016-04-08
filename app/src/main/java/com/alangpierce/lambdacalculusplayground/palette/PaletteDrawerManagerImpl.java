package com.alangpierce.lambdacalculusplayground.palette;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ScrollView;

public class PaletteDrawerManagerImpl implements PaletteDrawerManager {
    private static final int INITIAL_DRAWER_OPEN_DELAY_MS = 500;

    private final DrawerLayout lambdaPaletteDrawerRoot;
    private final ScrollView lambdaPaletteDrawer;

    private final DrawerLayout definitionPaletteDrawerRoot;
    private final ScrollView definitionPaletteDrawer;

    private final View fabContainer;

    private boolean isDestroyed = false;

    public PaletteDrawerManagerImpl(DrawerLayout lambdaPaletteDrawerRoot,
            ScrollView lambdaPaletteDrawer,
            DrawerLayout definitionPaletteDrawerRoot, ScrollView definitionPaletteDrawer,
            View fabContainer) {
        this.lambdaPaletteDrawerRoot = lambdaPaletteDrawerRoot;
        this.lambdaPaletteDrawer = lambdaPaletteDrawer;
        this.definitionPaletteDrawerRoot = definitionPaletteDrawerRoot;
        this.definitionPaletteDrawer = definitionPaletteDrawer;
        this.fabContainer = fabContainer;
    }

    @Override
    public void onCreateView(boolean isFirstTime) {
        lambdaPaletteDrawerRoot.addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                float offset = drawerView.getWidth() * slideOffset;
                fabContainer.setTranslationX(-offset);
            }
        });

        // If this is the first time opening the app, open the drawer after a short delay. This
        // makes it so the palette animates in, which emphasizes that it's a drawer and makes sure
        // the user starts with it visible.
        lambdaPaletteDrawerRoot.postDelayed(() -> {
            if (!isDestroyed) {
                lambdaPaletteDrawerRoot.openDrawer(GravityCompat.END);
            }
        }, INITIAL_DRAWER_OPEN_DELAY_MS);
    }

    @Override
    public void onViewStateRestored() {
        // Any drawer changes set the translation, but
        if (lambdaPaletteDrawerRoot.isDrawerOpen(lambdaPaletteDrawer)) {
            lambdaPaletteDrawer.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            fabContainer.setTranslationX(-lambdaPaletteDrawer.getMeasuredWidth());
        }
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
    }

    @Override
    public void toggleLambdaPalette() {
        if (lambdaPaletteDrawerRoot != null) {
            if (lambdaPaletteDrawerRoot.isDrawerOpen(GravityCompat.END)) {
                lambdaPaletteDrawerRoot.closeDrawer(GravityCompat.END);
            } else {
                lambdaPaletteDrawerRoot.openDrawer(GravityCompat.END);
            }
        }
    }

    @Override
    public void toggleDefinitionPalette() {
        if (definitionPaletteDrawerRoot != null) {
            if (definitionPaletteDrawerRoot.isDrawerOpen(GravityCompat.END)) {
                definitionPaletteDrawerRoot.closeDrawer(GravityCompat.END);
            } else {
                definitionPaletteDrawerRoot.openDrawer(GravityCompat.END);
            }
        }
    }
}
