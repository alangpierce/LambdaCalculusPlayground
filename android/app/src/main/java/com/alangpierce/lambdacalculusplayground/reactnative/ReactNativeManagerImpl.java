package com.alangpierce.lambdacalculusplayground.reactnative;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.alangpierce.lambdacalculusplayground.BuildConfig;
import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.facebook.react.shell.MainReactPackage;

import javax.annotation.Nullable;

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
                .addPackage(new ReactNativeDialogsPackage())
                .addPackage(new PlaygroundPackage(this))
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        reactRootView.startReactApplication(reactInstanceManager, "PlaygroundCanvas", null);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        reactRootView.setLayoutParams(layoutParams);
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
        activity.runOnUiThread(() -> reactInstanceManager.showDevOptionsDialog());
    }

    @Override
    public void reloadJs() {
        activity.runOnUiThread(() -> reactInstanceManager.getDevSupportManager().handleReloadJS());
    }

    @Override
    public void createLambda(String varName) {
        emit("createLambda", varName);
    }

    @Override
    public void createDefinition(String defName) {
        emit("createDefinition", defName);
    }

    @Override
    public void toggleLambdaPalette() {
        emit("toggleLambdaPalette", null);
    }

    @Override
    public void toggleDefinitionPalette() {
        emit("toggleDefinitionPalette", null);
    }

    @Override
    public void viewDemoVideo() {
        emit("viewDemoVideo", null);
    }

    private void emit(String eventName, @Nullable Object data) {
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
        if (reactContext != null) {
            RCTDeviceEventEmitter eventEmitter =
                    reactContext.getJSModule(RCTDeviceEventEmitter.class);
            eventEmitter.emit(eventName, data);
        }
    }
}
