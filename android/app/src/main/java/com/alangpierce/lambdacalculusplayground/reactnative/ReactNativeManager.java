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

    /**
     * Place a lambda with the given name in a nice place on the React Native canvas.
     */
    void createLambda(String varName);

    /**
     * Place a lambda with the given name in a nice place on the React Native canvas.
     */
    void createDefinition(String defName);

    void toggleLambdaPalette();
    void toggleDefinitionPalette();
    void viewDemoVideo();
}
