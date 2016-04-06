package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;

import java.util.Map.Entry;

public interface TopLevelExpressionState {
    Iterable<Entry<Integer, ScreenExpression>> expressionsById();
    Iterable<ScreenDefinition> definitions();
    void modifyExpression(int key, ScreenExpression expression);

    /**
     * Create or set the given definition.
     */
    void setDefinition(ScreenDefinition definition);
    void deleteExpression(int exprId);
    void deleteDefinition(String defName);

    int addScreenExpression(ScreenExpression screenExpression);

    PointDifference getPanOffset();
    void setPanOffset(PointDifference panOffset);

    void hydrateFromBundle(Bundle bundle);
    void persistToBundle(Bundle bundle);
}
