package com.alangpierce.lambdacalculusplayground;

import com.facebook.react.bridge.BaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class DeveloperSupportModule extends BaseJavaModule {
    private final ReactNativeManager reactNativeManager;

    public DeveloperSupportModule(ReactNativeManager reactNativeManager) {
        this.reactNativeManager = reactNativeManager;
    }

    @Override
    public String getName() {
        return "DeveloperSupportModule";
    }

    @ReactMethod
    public void showDevOptionsDialog() {
        reactNativeManager.showDevOptionsDialog();
    }

    @ReactMethod
    public void reloadJs() {
        reactNativeManager.reloadJs();
    }
}