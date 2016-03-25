package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;

import java.util.Map.Entry;

public interface TopLevelExpressionState {
    Iterable<Entry<Integer, ScreenExpression>> expressionsById();
    void modifyExpression(int key, ScreenExpression expression);
    void deleteExpression(int exprId);

    /**
     * Modify the state to create a new expression. Note that this does not do any rendering.
     */
    int addScreenExpression(ScreenExpression screenExpression);

    PointDifference getPanOffset();
    void setPanOffset(PointDifference panOffset);

    void hydrateFromBundle(Bundle bundle);
    void persistToBundle(Bundle bundle);
}
