package com.alangpierce.lambdacalculusplayground;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class PlaygroundPackage implements ReactPackage {
    private final DeveloperSupportProvider developerSupportProvider;

    public PlaygroundPackage(DeveloperSupportProvider developerSupportProvider) {
        this.developerSupportProvider = developerSupportProvider;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return ImmutableList.of(
                new DeveloperSupportModule(developerSupportProvider)
        );
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return ImmutableList.of();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return ImmutableList.of();
    }
}
