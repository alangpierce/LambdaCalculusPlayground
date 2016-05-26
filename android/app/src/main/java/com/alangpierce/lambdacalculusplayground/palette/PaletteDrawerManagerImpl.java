package com.alangpierce.lambdacalculusplayground.palette;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ScrollView;

import javax.annotation.Nullable;

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

    // The final state that we should animate toward.
    private State targetState = State.CLOSED;
    // Keep track of any listener being used for animation. There will only be at most one, so for
    // now it can just be a nullable variable.
    private @Nullable DrawerListener existingAnimationListener = null;

    private enum State {
        CLOSED,
        LAMBDA_OPEN,
        DEFINITION_OPEN
    }

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
//        if (isFirstTime) {
//            lambdaPaletteDrawerRoot.postDelayed(() -> {
//                if (!isDestroyed) {
//                    targetState = State.LAMBDA_OPEN;
//                    animateToTargetState();
//                }
//            }, INITIAL_DRAWER_OPEN_DELAY_MS);
//        }

        lambdaPaletteView.onCreateView();
        definitionPaletteView.onCreateView();
    }

    @Override
    public void onViewStateRestored() {
        recomputeState();
    }

    @Override
    public void onPaletteContentsChanged() {
        recomputeState();
    }

    private void recomputeState() {
        // Any drawer changes set the translation, but we need to recompute the translation on
        // rotate (or restore for another reason) since we don't get a change event.
        if (lambdaPaletteDrawerRoot.isDrawerOpen(GravityCompat.END)) {
            lambdaPaletteDrawer.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            lambdaPaletteOffsetPixels = lambdaPaletteDrawer.getMeasuredWidth();
            targetState = State.LAMBDA_OPEN;
        }
        if (definitionPaletteDrawerRoot.isDrawerOpen(GravityCompat.END)) {
            definitionPaletteDrawer.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            definitionPaletteOffsetPixels = definitionPaletteDrawer.getMeasuredWidth();
            targetState = State.DEFINITION_OPEN;
        }
        repositionFabs();
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
        if (targetState == State.LAMBDA_OPEN) {
            targetState = State.CLOSED;
        } else {
            targetState = State.LAMBDA_OPEN;
        }
        animateToTargetState();
    }

    @Override
    public void toggleDefinitionPalette() {
        if (targetState == State.DEFINITION_OPEN) {
            targetState = State.CLOSED;
        } else {
            targetState = State.DEFINITION_OPEN;
        }
        animateToTargetState();
    }

    private void animateToTargetState() {
        if (existingAnimationListener != null) {
            // We don't know or care which drawer has a listener or if the listener already removed
            // itself; just remove it from both, which will be a no-op in one or both cases.
            lambdaPaletteDrawerRoot.removeDrawerListener(existingAnimationListener);
            definitionPaletteDrawerRoot.removeDrawerListener(existingAnimationListener);
            existingAnimationListener = null;
        }

        if (targetState == State.CLOSED) {
            lambdaPaletteDrawerRoot.closeDrawer(GravityCompat.END);
            definitionPaletteDrawerRoot.closeDrawer(GravityCompat.END);
        } else if (targetState == State.LAMBDA_OPEN) {
            openDrawer(lambdaPaletteDrawerRoot, definitionPaletteDrawerRoot);
        } else if (targetState == State.DEFINITION_OPEN) {
            openDrawer(definitionPaletteDrawerRoot, lambdaPaletteDrawerRoot);
        }
    }

    private void openDrawer(DrawerLayout drawerToOpen, DrawerLayout otherDrawer) {
        if (otherDrawer.isDrawerVisible(GravityCompat.END)) {
            otherDrawer.closeDrawer(GravityCompat.END);
            SimpleDrawerListener listener = new SimpleDrawerListener() {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    if (slideOffset < 0.5) {
                        otherDrawer.removeDrawerListener(this);
                        drawerToOpen.openDrawer(GravityCompat.END);
                    }
                }
            };
            otherDrawer.addDrawerListener(listener);
            existingAnimationListener = listener;
        } else {
            drawerToOpen.openDrawer(GravityCompat.END);
        }
    }
}
