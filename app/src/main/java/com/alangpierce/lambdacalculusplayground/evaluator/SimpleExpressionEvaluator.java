package com.alangpierce.lambdacalculusplayground.evaluator;

import com.alangpierce.lambdacalculusplayground.expression.Expression;
import com.alangpierce.lambdacalculusplayground.expression.Expressions;

public class SimpleExpressionEvaluator implements ExpressionEvaluator {
    @Override
    public Expression evaluate(Expression expression) {
        for (int i = 0; i < 1000; i++) {
            Expression next = Expressions.step(expression);
            if (next == null) {
                break;
            } else {
                expression = next;
            }
        }
        return Expressions.normalizeNames(expression);
    }
}
