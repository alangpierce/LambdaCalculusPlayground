package com.alangpierce.lambdacalculusplayground.evaluator;

/**
 * A slot is a reference to evaluation that may or may not need to happen.
 */
public class Slot {
    // When a slot is evaluated, isValue changes to true and expr changes to the evaluated value.
    public boolean isValue;
    public EvalExpression expr;

    public Slot(boolean isValue, EvalExpression expr) {
        this.isValue = isValue;
        this.expr = expr;
    }

    public static Slot create(EvalExpression unevaluatedExpr) {
        return new Slot(false, unevaluatedExpr);
    }
}
