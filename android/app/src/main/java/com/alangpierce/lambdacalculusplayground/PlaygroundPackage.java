package com.alangpierce.lambdacalculusplayground;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlaygroundPackage implements ReactPackage {
    private final DeveloperSupportProvider developerSupportProvider;

    public PlaygroundPackage(DeveloperSupportProvider developerSupportProvider) {
        this.developerSupportProvider = developerSupportProvider;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(
                new DeveloperSupportModule(developerSupportProvider)
        );
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}
