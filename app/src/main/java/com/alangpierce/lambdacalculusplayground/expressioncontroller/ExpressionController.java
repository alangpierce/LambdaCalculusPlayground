package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import android.view.View;
import android.widget.LinearLayout;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

public interface ExpressionController {
    LinearLayout getView();
    void setCallbacks(OnChangeCallback onChangeCallback, OnDetachCallback onDetachCallback);

    /**
     * Callback used for expressions to propagate changes in the actual backing UserExpression. For
     * example, dragging an expression out of a larger expression will cause OnChange to propagate
     * up to the top level, where it will be stored in the fragment's state and stored to the bundle
     * if necessary.
     */
    interface OnChangeCallback {
        void onChange(UserExpression newExpression);
    }

    /**
     * Used to indicate to the parent that we should be removed.
     */
    interface OnDetachCallback {
        void onDetach(View viewToDetach);
    }
}
