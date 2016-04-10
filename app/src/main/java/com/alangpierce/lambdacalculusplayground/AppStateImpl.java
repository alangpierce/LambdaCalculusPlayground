package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import com.alangpierce.lambdacalculusplayground.geometry.PointDifference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Stateful class for keeping track of the full set of expressions.
 *
 * This class is just a field on the fragment, so its lifetime is that of the fragment, which is
 * slightly longer than the fragment's view.
 */
public class AppStateImpl implements AppState {
    /*
     * We keep expressions with IDs so that we can delete and modify them later as necessary, but
     * the bundled format is just a list of ScreenExpressions. Same for definitions.
     * TODO: Maybe we could consolidate the redundant code with expressions and definitions.
     */
    private Map<Integer, ScreenExpression> expressions = Maps.newConcurrentMap();
    private Map<String, ScreenDefinition> definitions = Maps.newConcurrentMap();
    private AtomicInteger maxId = new AtomicInteger();
    private AtomicReference<PointDifference> panOffset =
            new AtomicReference<>(PointDifference.create(0, 0));

    @Override
    public Iterable<Entry<Integer, ScreenExpression>> expressionsById() {
        return expressions.entrySet();
    }

    @Override
    public Iterable<ScreenDefinition> definitions() {
        return definitions.values();
    }

    @Override
    public void modifyExpression(int exprId, ScreenExpression expression) {
        expressions.put(exprId, expression);
    }

    @Override
    public void setDefinition(ScreenDefinition definition) {
        definitions.put(definition.defName(), definition);
    }

    @Override
    public void deleteExpression(int exprId) {
        expressions.remove(exprId);
    }

    @Override
    public void deleteDefinition(String defName) {
        definitions.remove(defName);
    }

    @Override
    public int addScreenExpression(ScreenExpression screenExpression) {
        int exprId = maxId.incrementAndGet();
        expressions.put(exprId, screenExpression);
        return exprId;
    }

    @Override
    public PointDifference getPanOffset() {
        return panOffset.get();
    }

    @Override
    public void setPanOffset(PointDifference panOffset) {
        this.panOffset.set(panOffset);
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
        List<ScreenDefinition> screenDefinitions =
                (List<ScreenDefinition>) bundle.getSerializable("definitions");
        if (screenDefinitions != null) {
            for (ScreenDefinition definition : screenDefinitions) {
                setDefinition(definition);
            }
        }
        PointDifference panOffset = (PointDifference) bundle.getSerializable("panOffset");
        if (panOffset != null) {
            this.panOffset.set(panOffset);
        }
    }

    @Override
    public void persistToBundle(Bundle bundle) {
        bundle.putSerializable("expressions", ImmutableList.copyOf(expressions.values()));
        bundle.putSerializable("definitions", ImmutableList.copyOf(definitions.values()));
        bundle.putSerializable("panOffset", panOffset.get());
    }
}
