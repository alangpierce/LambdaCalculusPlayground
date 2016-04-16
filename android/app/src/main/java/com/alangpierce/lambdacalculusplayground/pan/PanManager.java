package com.alangpierce.lambdacalculusplayground.pan;

import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;

public interface PanManager {
    void init(PointDifference initialPanOffset);
    void registerPanListener(PanListener panListener);
    void unregisterPanListener(PanListener panListener);
    PointDifference getPanOffset();

    // Callback that is called whenever a pan event happens.
    interface PanListener {
        void onPan();
    }
}
