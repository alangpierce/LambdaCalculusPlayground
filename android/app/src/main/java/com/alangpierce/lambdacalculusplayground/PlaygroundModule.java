package com.alangpierce.lambdacalculusplayground;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManager;
import com.alangpierce.lambdacalculusplayground.reactnative.ReactNativeManagerImpl;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Module;
import dagger.Provides;

@Module
public class PlaygroundModule {
    private final Activity activity;

    // These are all children of the fragment.
    @Bind(R.id.above_palette_root) RelativeLayout abovePaletteRoot;
    @Bind(R.id.canvas_root) RelativeLayout canvasRoot;

    private PlaygroundModule(Activity activity) {
        this.activity = activity;
    }

    public static PlaygroundModule create(Activity activity, View root) {
        PlaygroundModule module = new PlaygroundModule(activity);
        ButterKnife.bind(module, root);
        return module;
    }

    @Provides Activity provideActivity() {
        return activity;
    }

    @Provides Context provideContext(Activity activity) {
        return activity;
    }

    @Provides
    LayoutInflater provideLayoutInflater(Activity activity) {
        return activity.getLayoutInflater();
    }

    @Qualifier @interface CanvasRoot {}
    @Provides @CanvasRoot
    RelativeLayout provideCanvasRoot() {
        return canvasRoot;
    }

    @Qualifier @interface AbovePaletteRoot {}
    @Provides @AbovePaletteRoot
    RelativeLayout provideAbovePaletteRoot() {
        return abovePaletteRoot;
    }

    @Provides @Singleton
    ReactNativeManager provideReactNativeManager() {
        return new ReactNativeManagerImpl(canvasRoot, activity);
    }
}
