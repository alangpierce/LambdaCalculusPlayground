package com.alangpierce.lambdacalculusplayground.reactnative;

/**
 * Interface to all React Native code.
 */
public interface ReactNativeManager {
    void init();
    void onPause();
    void onResume();
    void showDevOptionsDialog();
    void reloadJs();
    void invalidateState();
}
