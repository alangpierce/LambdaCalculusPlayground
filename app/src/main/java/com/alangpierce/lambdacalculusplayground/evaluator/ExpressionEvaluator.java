package com.alangpierce.lambdacalculusplayground.evaluator;

import com.alangpierce.lambdacalculusplayground.expression.Expression;

public interface ExpressionEvaluator {
    /**
     * Step an expression until it is a value.
     */
    Expression evaluate(Expression expression);
}
