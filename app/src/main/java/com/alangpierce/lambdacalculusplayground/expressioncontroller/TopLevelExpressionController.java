package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.ScreenExpression;

public interface TopLevelExpressionController {
    LinearLayout getView();
    void setCallbacks(OnTopLevelChangeCallback onChangeCallback,
                      OnTopLevelDetachCallback onDetachCallback);

    interface OnTopLevelChangeCallback {
        void onChange(ScreenExpression newScreenExpression);
    }

    interface OnTopLevelDetachCallback {
        void onDetach(View viewToDetach);
    }


}
