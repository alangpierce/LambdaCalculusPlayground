package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.widget.LinearLayout;

public interface ExpressionController {
    LinearLayout getView();
    void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback);
}
