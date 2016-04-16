package com.alangpierce.lambdacalculusplayground.component;

import android.view.View;

public interface ComponentParent {
    void detach(View view);
    void attach(View view);
}
