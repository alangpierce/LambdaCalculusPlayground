package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import java.util.Map.Entry;

public interface TopLevelExpressionState {
    Iterable<Entry<Integer, ScreenExpression>> expressionsById();
    void modifyExpression(int key, ScreenExpression expression);
    void deleteExpression(int exprId);

    /**
     * Modify the state to create a new expression. Note that this does not do any rendering.
     */
    int addScreenExpression(ScreenExpression screenExpression);
    void hydrateFromBundle(Bundle bundle);
    void persistToBundle(Bundle bundle);
}
