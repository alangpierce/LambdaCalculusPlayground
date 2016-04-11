package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;

import java.util.Map;
import java.util.Map.Entry;

public interface AppState {
    Iterable<Entry<Integer, ScreenExpression>> expressionsById();
    Map<String, CanvasPoint> getDefinitionsOnScreen();
    Map<String, UserExpression> getAllDefinitions();


    void modifyExpression(int key, ScreenExpression expression);
    void deleteExpression(int exprId);
    int addScreenExpression(ScreenExpression screenExpression);

    /**
     * Create or set the given definition.
     */
    void setDefinition(String defName, UserExpression userExpression);
    void addDefinitionOnScreen(String defName, CanvasPoint point);
    void removeDefinitionFromScreen(String defName);
    void deleteDefinition(String defName);

    boolean isAutomaticNumbersEnabled();
    void setEnableAutomaticNumbers(boolean enableAutomaticNumbers);

    PointDifference getPanOffset();
    void setPanOffset(PointDifference panOffset);

    void hydrateFromBundle(Bundle bundle);
    void persistToBundle(Bundle bundle);
}
