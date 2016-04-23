package com.alangpierce.lambdacalculusplayground.reactnative;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import com.alangpierce.lambdacalculusplayground.BuildConfig;
import com.facebook.react.LifecycleState;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
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

        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, screenSize.y / 2);
        layoutParams.topMargin = screenSize.y / 2;
        reactRootView.setLayoutParams(layoutParams);
        reactRootView.setBackgroundColor(Color.GRAY);
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

    @Override
    public void createLambda(String varName) {
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
        if (reactContext != null) {
            RCTDeviceEventEmitter eventEmitter =
                    reactContext.getJSModule(RCTDeviceEventEmitter.class);
            eventEmitter.emit("createLambda", varName);
        }
    }
}
