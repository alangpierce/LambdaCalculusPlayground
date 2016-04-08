package com.alangpierce.lambdacalculusplayground.palette;

/**
 * Class for dealing with the opening and closing of the palette drawers, and the repositioning
 * of the FABs as a result.
 */
public interface PaletteDrawerManager {
    void onCreateView(boolean isFirstTime);
    void onViewStateRestored();
    void onDestroyView();
    void toggleLambdaPalette();
    void toggleDefinitionPalette();

}
