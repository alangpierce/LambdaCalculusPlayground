package com.alangpierce.lambdacalculusplayground.reactnative;

import android.app.Activity;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.BuildConfig;
import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.shell.MainReactPackage;

public class ReactNativeManagerImpl implements ReactNativeManager {
    private final RelativeLayout canvasRoot;
    private final Activity activity;

    private ReactInstanceManager reactInstanceManager;

    public ReactNativeManagerImpl(RelativeLayout canvasRoot, Activity activity) {
        this.canvasRoot = canvasRoot;
        this.activity = activity;
    }

    @Override
    public void init() {
        ReactRootView reactRootView = new ReactRootView(activity);
        reactInstanceManager = ReactInstanceManager.builder()
                .setApplication(activity.getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModuleName("index.android")
                .addPackage(new MainReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        reactRootView.startReactApplication(reactInstanceManager, "PlaygroundCanvas", null);
        canvasRoot.addView(reactRootView);
    }

    @Override
    public void onResume() {
        reactInstanceManager.onHostResume(activity, () -> {});
    }

    @Override
    public void onPause() {
        reactInstanceManager.onHostPause();
    }

    @Override
    public void showDevOptionsDialog() {
        reactInstanceManager.showDevOptionsDialog();
    }

    @Override
    public void reloadJs() {
        reactInstanceManager.getDevSupportManager().handleReloadJS();
    }
}
