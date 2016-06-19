package com.alangpierce.lambdacalculusplayground;

import com.aakashns.reactnativedialogs.ReactNativeDialogsPackage;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;


/**
 * React native hard-codes the name MainActivity for the run-android command, so just call it that.
 * TODO: Consider fixing this or just changing it back.
 */
public class MainActivity extends ReactActivity implements DeveloperSupportProvider {
    private ReactInstanceManager reactInstanceManager;

    @Override
    protected ReactInstanceManager createReactInstanceManager() {
        reactInstanceManager = super.createReactInstanceManager();
        return reactInstanceManager;
    }

    @Override
    protected String getMainComponentName() {
        return "PlaygroundCanvas";
    }

    @Override
    protected boolean getUseDeveloperSupport() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.asList(
                new MainReactPackage(),
                new ReactNativeDialogsPackage(),
                new PlaygroundPackage(this)
        );
    }

    @Override
    public void showDevOptionsDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reactInstanceManager.showDevOptionsDialog();
            }
        });
    }

    @Override
    public void reloadJs() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reactInstanceManager.getDevSupportManager().handleReloadJS();
            }
        });
    }
}
