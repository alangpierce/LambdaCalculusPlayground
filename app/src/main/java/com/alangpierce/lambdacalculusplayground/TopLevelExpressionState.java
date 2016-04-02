package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;

import java.util.Map.Entry;

public interface TopLevelExpressionState {
    Iterable<Entry<Integer, ScreenExpression>> expressionsById();
    Iterable<Entry<Integer, ScreenDefinition>> definitionsById();
    void modifyExpression(int key, ScreenExpression expression);
    void modifyDefinition(int key, ScreenDefinition definition);
    void deleteExpression(int exprId);
    void deleteDefinition(int defId);

    int addScreenExpression(ScreenExpression screenExpression);
    int addScreenDefinition(ScreenDefinition screenDefinition);

    PointDifference getPanOffset();
    void setPanOffset(PointDifference panOffset);

    void hydrateFromBundle(Bundle bundle);
    void persistToBundle(Bundle bundle);
}
