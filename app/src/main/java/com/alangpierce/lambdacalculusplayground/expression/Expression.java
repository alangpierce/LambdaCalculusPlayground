package com.alangpierce.lambdacalculusplayground.expression;

public interface Expression {
    <T> T visit(ExpressionVisitor<T> visitor);

    public interface ExpressionVisitor<T> {
        T visit(Lambda lambda);
        T visit(FuncCall funcCall);
        T visit(Variable variable);
    }
}
