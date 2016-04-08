package com.alangpierce.lambdacalculusplayground.palette;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ScrollView;

public class PaletteDrawerManagerImpl implements PaletteDrawerManager {
    private static final int INITIAL_DRAWER_OPEN_DELAY_MS = 500;

    private final PaletteView lambdaPaletteView;
    private final PaletteView definitionPaletteView;

    private final DrawerLayout lambdaPaletteDrawerRoot;
    private final ScrollView lambdaPaletteDrawer;

    private final DrawerLayout definitionPaletteDrawerRoot;
    private final ScrollView definitionPaletteDrawer;

    private final View fabContainer;

    // Keep track of the position of both drawers, since the drawer class doesn't expose these
    // directly. The FABs should be at the min of these two positions on the screen.
    private float lambdaPaletteOffsetPixels = 0;
    private float definitionPaletteOffsetPixels = 0;

    private boolean isDestroyed = false;

    public PaletteDrawerManagerImpl(
            PaletteView lambdaPaletteView,
            PaletteView definitionPaletteView,
            DrawerLayout lambdaPaletteDrawerRoot, ScrollView lambdaPaletteDrawer,
            DrawerLayout definitionPaletteDrawerRoot, ScrollView definitionPaletteDrawer,
            View fabContainer) {
        this.lambdaPaletteView = lambdaPaletteView;
        this.definitionPaletteView = definitionPaletteView;
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
                lambdaPaletteOffsetPixels = drawerView.getWidth() * slideOffset;
                repositionFabs();
            }
        });
        definitionPaletteDrawerRoot.addDrawerListener(new SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                definitionPaletteOffsetPixels = drawerView.getWidth() * slideOffset;
                repositionFabs();
            }
        });

        // If this is the first time opening the app, open the drawer after a short delay. This
        // makes it so the palette animates in, which emphasizes that it's a drawer and makes sure
        // the user starts with it visible.
        if (isFirstTime) {
            lambdaPaletteDrawerRoot.postDelayed(() -> {
                if (!isDestroyed) {
                    lambdaPaletteDrawerRoot.openDrawer(GravityCompat.END);
                }
            }, INITIAL_DRAWER_OPEN_DELAY_MS);
        }

        lambdaPaletteView.onCreateView();
        definitionPaletteView.onCreateView();
    }

    @Override
    public void onViewStateRestored() {
        // Any drawer changes set the translation, but we need to recompute the translation on
        // rotate (or restore for another reason) since we don't get a change event.
        if (lambdaPaletteDrawerRoot.isDrawerOpen(lambdaPaletteDrawer)) {
            lambdaPaletteDrawer.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            fabContainer.setTranslationX(-lambdaPaletteDrawer.getMeasuredWidth());
        }
    }

    private void repositionFabs() {
        fabContainer.setTranslationX(
                Math.min(-lambdaPaletteOffsetPixels, -definitionPaletteOffsetPixels));
    }

    @Override
    public void onDestroyView() {
        isDestroyed = true;
    }

    @Override
    public void toggleLambdaPalette() {
        toggle(lambdaPaletteDrawerRoot, definitionPaletteDrawerRoot);
    }

    @Override
    public void toggleDefinitionPalette() {
        toggle(definitionPaletteDrawerRoot, lambdaPaletteDrawerRoot);
    }

    private void toggle(DrawerLayout toggleDrawer, DrawerLayout otherDrawer) {
        if (otherDrawer.isDrawerOpen(GravityCompat.END)) {
            otherDrawer.closeDrawer(GravityCompat.END);
            otherDrawer.addDrawerListener(new SimpleDrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    if (slideOffset < 0.5) {
                        otherDrawer.removeDrawerListener(this);
                        toggleDrawer.openDrawer(GravityCompat.END);
                    }
                }
            });
        } else if (toggleDrawer.isDrawerOpen(GravityCompat.END)) {
            toggleDrawer.closeDrawer(GravityCompat.END);
        } else {
            toggleDrawer.openDrawer(GravityCompat.END);
        }
    }
}
