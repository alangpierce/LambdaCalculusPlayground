package com.alangpierce.lambdacalculusplayground.expressioncontroller;

import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

/**
 * Callback used for expressions to propagate changes in the actual backing UserExpression. For
 * example, dragging an expression out of a larger expression will cause OnChange to propagate up to
 * the top level, where it will be stored in the fragment's state and stored to the bundle if
 * necessary.
 */
public interface OnChangeCallback {
    void onChange(UserExpression newExpression);
}
