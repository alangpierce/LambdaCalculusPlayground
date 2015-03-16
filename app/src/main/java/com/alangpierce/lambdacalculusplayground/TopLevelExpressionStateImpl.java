package com.alangpierce.lambdacalculusplayground;

import android.os.Bundle;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import autovalue.shaded.com.google.common.common.collect.ImmutableList;
import autovalue.shaded.com.google.common.common.collect.Maps;

/**
 * Stateful class for keeping track of the full set of expressions.
 *
 * This class is just a field on the fragment, so its lifetime is that of the fragment, which is
 * slightly longer than the fragment's view.
 */
public class TopLevelExpressionStateImpl implements TopLevelExpressionState {
    /*
     * We keep expressions with IDs so that we can delete and modify them later as necessary, but
     * the bundled format is just a list of ScreenExpressions.
     */
    private Map<Integer, ScreenExpression> expressions = Maps.newConcurrentMap();
    private AtomicInteger maxId = new AtomicInteger();

    @Override
    public Iterable<Entry<Integer, ScreenExpression>> expressionsById() {
        return expressions.entrySet();
    }

    @Override
    public void modifyExpression(int exprId, ScreenExpression expression) {
        expressions.put(exprId, expression);
    }

    @Override
    public void deleteExpression(int exprId) {
        expressions.remove(exprId);
    }

    @Override
    public int addScreenExpression(ScreenExpression screenExpression) {
        int exprId = maxId.incrementAndGet();
        expressions.put(exprId, screenExpression);
        return exprId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void hydrateFromBundle(Bundle bundle) {
        List<ScreenExpression> screenExpressions =
                (List<ScreenExpression>)bundle.getSerializable("expressions");
        for (ScreenExpression expression : screenExpressions) {
            addScreenExpression(expression);
        }
    }

    @Override
    public void persistToBundle(Bundle bundle) {
        bundle.putSerializable("expressions", ImmutableList.copyOf(expressions.values()));
    }
}
