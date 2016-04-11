package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import com.alangpierce.lambdacalculusplayground.geometry.CanvasPoint;
import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.alangpierce.lambdacalculusplayground.userexpression.UserExpression;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Stateful class for keeping track of the full set of expressions.
 *
 * This class is just a field on the fragment, so its lifetime is that of the fragment, which is
 * slightly longer than the fragment's view.
 */
public class AppStateImpl implements AppState {
    /*
     * We keep expressions with IDs so that we can delete and modify them later as necessary, but
     * the bundled format is just a list of ScreenExpressions.
     */
    private Map<Integer, ScreenExpression> expressions = new HashMap<>();

    private Map<String, UserExpression> allDefinitions = new HashMap<>();
    private Map<String, CanvasPoint> definitionsOnScreen = new HashMap<>();

    private int maxExprId = 0;
    private PointDifference panOffset = PointDifference.create(0, 0);

    @Override
    public Iterable<Entry<Integer, ScreenExpression>> expressionsById() {
        return expressions.entrySet();
    }

    @Override
    public Map<String, CanvasPoint> getDefinitionsOnScreen() {
        return definitionsOnScreen;
    }

    @Override
    public Map<String, UserExpression> getAllDefinitions() {
        return allDefinitions;
    }

    @Override
    public void modifyExpression(int exprId, ScreenExpression expression) {
        expressions.put(exprId, expression);
    }

    @Override
    public void setDefinition(String defName, UserExpression userExpression) {
        allDefinitions.put(defName, userExpression);
    }

    @Override
    public void addDefinitionOnScreen(String defName, CanvasPoint point) {
        definitionsOnScreen.put(defName, point);
    }

    @Override
    public void removeDefinitionFromScreen(String defName) {
        definitionsOnScreen.remove(defName);
    }

    @Override
    public void deleteDefinition(String defName) {
        allDefinitions.remove(defName);
    }

    @Override
    public void deleteExpression(int exprId) {
        expressions.remove(exprId);
    }

    @Override
    public int addScreenExpression(ScreenExpression screenExpression) {
        int exprId = maxExprId;
        maxExprId++;
        expressions.put(exprId, screenExpression);
        return exprId;
    }

    @Override
    public PointDifference getPanOffset() {
        return panOffset;
    }

    @Override
    public void setPanOffset(PointDifference panOffset) {
        this.panOffset = panOffset;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void hydrateFromBundle(Bundle bundle) {
        List<ScreenExpression> screenExpressions =
                (List<ScreenExpression>)bundle.getSerializable("expressions");
        if (screenExpressions != null) {
            for (ScreenExpression expression : screenExpressions) {
                addScreenExpression(expression);
            }
        }

        Map<String, UserExpression> allDefinitionsInput =
                (Map<String, UserExpression>) bundle.getSerializable("allDefinitions");
        if (allDefinitionsInput != null) {
            allDefinitions.putAll(allDefinitionsInput);
        }

        Map<String, CanvasPoint> definitionsOnScreenInput =
                (Map<String, CanvasPoint>) bundle.getSerializable("definitionsOnScreen");
        if (definitionsOnScreenInput != null) {
            definitionsOnScreen.putAll(definitionsOnScreenInput);
        }

        PointDifference panOffset = (PointDifference) bundle.getSerializable("panOffset");
        if (panOffset != null) {
            this.panOffset = panOffset;
        }
    }

    @Override
    public void persistToBundle(Bundle bundle) {
        // TODO: Use parcelables instead.
        bundle.putSerializable("expressions", ImmutableList.copyOf(expressions.values()));
        bundle.putSerializable("allDefinitions", (Serializable) allDefinitions);
        bundle.putSerializable("definitionsOnScreen", (Serializable) definitionsOnScreen);
        bundle.putSerializable("panOffset", panOffset);
    }
}
