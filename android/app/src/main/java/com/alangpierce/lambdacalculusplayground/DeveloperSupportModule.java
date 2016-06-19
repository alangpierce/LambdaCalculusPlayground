package com.alangpierce.lambdacalculusplayground;

import com.facebook.react.bridge.BaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class DeveloperSupportModule extends BaseJavaModule {
    private final DeveloperSupportProvider developerSupportProvider;

    public DeveloperSupportModule(DeveloperSupportProvider developerSupportProvider) {
        this.developerSupportProvider = developerSupportProvider;
    }

    @Override
    public String getName() {
        return "DeveloperSupportModule";
    }

    @ReactMethod
    public void showDevOptionsDialog() {
        developerSupportProvider.showDevOptionsDialog();
    }

    @ReactMethod
    public void reloadJs() {
        developerSupportProvider.reloadJs();
    }
}